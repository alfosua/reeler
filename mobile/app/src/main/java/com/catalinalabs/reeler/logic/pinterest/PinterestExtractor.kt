package com.catalinalabs.reeler.logic.pinterest

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.core.ExtractionException
import com.catalinalabs.reeler.logic.core.Extractor
import com.catalinalabs.reeler.logic.core.ReelerHttp
import com.catalinalabs.reeler.logic.getFilenameForMedia
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.userAgent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pinterest pins (videos and images), using the same unauthenticated
 * PinResource endpoint yt-dlp's PinterestIE queries.
 */
object PinterestExtractor : Extractor {
    override val name = "pinterest"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:[a-z]{2,3}\\.)?(?:pinterest\\.(?:com|[a-z]{2,3})|pin\\.it)/"
    )

    override suspend fun extract(url: String): MediaInfoExtraction {
        val pinId = extractPinIdFromUrl(url)
            ?: throw IllegalArgumentException("Invalid Pinterest URL")
        val pin = fetchPinData(pinId, url)
        val caption = (pin.title?.takeIf { it.isNotBlank() }
            ?: pin.description?.takeIf { it.isNotBlank() })
            ?.split('\n')?.get(0)?.trim()
        val pinner = pin.pinner
        val author = AuthorExtraction(
            username = pinner?.username,
            avatarUrl = pinner?.imageSmallUrl,
            profileUrl = pinner?.username?.let { "https://www.pinterest.com/$it/" },
            name = pinner?.fullName ?: pinner?.username,
            userId = pinner?.username,
        )
        val image = pin.images?.orig

        val videoList = pin.videos?.videoList ?: pin.storyPinData?.pages
            ?.firstOrNull()?.blocks?.firstOrNull()?.video?.videoList
        val video = videoList?.let {
            it.v720p ?: it.vExp7 ?: it.values.filterNotNull().maxByOrNull { v -> v.width ?: 0 }
        }
        if (video?.url != null && !video.url.endsWith(".m3u8")) {
            return MediaInfoExtraction(
                type = MediaType.Video,
                sourceUrl = url,
                source = name,
                width = video.width,
                height = video.height,
                duration = video.duration?.div(1000.0),
                caption = caption,
                thumbnailUrl = video.thumbnail ?: image?.url,
                author = author,
                download = MediaDownloadableExtraction(
                    contentType = ContentType.Video.MP4.toString(),
                    url = video.url,
                    referer = "https://www.pinterest.com/",
                    filename = getFilenameForMedia(caption, pinId),
                ),
            )
        }

        if (image?.url != null) {
            val ext = image.url.substringAfterLast('.').substringBefore('?')
                .takeIf { it.length in 3..4 } ?: "jpg"
            return MediaInfoExtraction(
                type = MediaType.Image,
                sourceUrl = url,
                source = name,
                width = image.width,
                height = image.height,
                caption = caption,
                thumbnailUrl = image.url,
                author = author,
                download = MediaDownloadableExtraction(
                    contentType = if (ext == "png") {
                        ContentType.Image.PNG.toString()
                    } else {
                        ContentType.Image.JPEG.toString()
                    },
                    url = image.url,
                    referer = "https://www.pinterest.com/",
                    filename = getFilenameForMedia(caption, pinId, extension = ext),
                ),
            )
        }

        throw ExtractionException("This pin has no downloadable video or image.")
    }

    private fun extractPinIdFromUrl(url: String): String? {
        return Regex("pinterest\\.[a-z.]+/pin/(?:[\\w-]+--)?(\\d+)").find(url)
            ?.groupValues?.getOrNull(1)
    }

    private suspend fun fetchPinData(pinId: String, sourceUrl: String): PinData {
        val data = "{\"options\":{\"id\":\"$pinId\",\"field_set_key\":\"unauth_react_main_pin\"}}"
        val requestUrl = "$RESOURCE_URL/?source_url=${sourceUrl.encodeURLParameter()}" +
                "&data=${data.encodeURLParameter()}"
        val response = ReelerHttp.client.get(requestUrl) {
            headers {
                userAgent(ReelerHttp.DEFAULT_USER_AGENT)
                append("Accept", "application/json")
                append("X-Pinterest-PWS-Handler", "www/pin/[id].js")
            }
        }
        val body: String = response.body()
        val raw = ReelerHttp.json.decodeFromString<RawData>(body)
        return raw.resourceResponse?.data
            ?: throw ExtractionException("Pinterest did not return pin data.")
    }

    private const val RESOURCE_URL =
        "https://www.pinterest.com/resource/PinResource/get"
}

@Serializable
private data class RawData(
    @SerialName("resource_response")
    val resourceResponse: ResourceResponse? = null,
)

@Serializable
private data class ResourceResponse(
    val data: PinData? = null,
)

@Serializable
private data class PinData(
    val title: String? = null,
    val description: String? = null,
    val pinner: Pinner? = null,
    val images: Images? = null,
    val videos: Videos? = null,
    @SerialName("story_pin_data")
    val storyPinData: StoryPinData? = null,
)

@Serializable
private data class Pinner(
    val username: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("image_small_url")
    val imageSmallUrl: String? = null,
)

@Serializable
private data class Images(
    val orig: ImageVariant? = null,
)

@Serializable
private data class ImageVariant(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
private data class Videos(
    @SerialName("video_list")
    val videoList: VideoList? = null,
)

@Serializable
private data class VideoList(
    @SerialName("V_720P")
    val v720p: VideoVariant? = null,
    @SerialName("V_EXP7")
    val vExp7: VideoVariant? = null,
) {
    val values: List<VideoVariant?>
        get() = listOf(v720p, vExp7)
}

@Serializable
private data class VideoVariant(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val thumbnail: String? = null,
)

@Serializable
private data class StoryPinData(
    val pages: List<StoryPage>? = null,
)

@Serializable
private data class StoryPage(
    val blocks: List<StoryBlock>? = null,
)

@Serializable
private data class StoryBlock(
    val video: Videos? = null,
)
