package com.catalinalabs.reeler.logic.tiktok

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.core.ExtractionException
import com.catalinalabs.reeler.logic.core.Extractor
import com.catalinalabs.reeler.logic.core.ReelerHttp
import com.catalinalabs.reeler.logic.getFilenameForMedia
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.setCookie
import io.ktor.http.userAgent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object TiktokExtractor : Extractor {
    override val name = "tiktok"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:www\\.|m\\.|vm\\.|vt\\.)?tiktok\\.com/"
    )

    override suspend fun extract(url: String): MediaInfoExtraction {
        val (rawData, cookie) = fetchRawData(url)
        val videoDetail = rawData.defaultScope?.videoDetail
        val itemStruct = videoDetail?.itemInfo?.itemStruct
            ?: throw ExtractionException("TikTok did not return media info; the post may be private or removed.")
        val video = itemStruct.video
        val author = itemStruct.author
        val uniqueId = author?.uniqueId
        val caption = (videoDetail.shareMeta?.desc ?: itemStruct.desc)
            ?.split('\n')?.get(0)
        val authorInfo = AuthorExtraction(
            username = uniqueId,
            avatarUrl = author?.avatarThumb,
            profileUrl = uniqueId?.let { "$BASE_URL/@${it}/" },
            name = uniqueId?.let { "@${it}" },
            userId = uniqueId,
        )

        val images = itemStruct.imagePost?.images
        if (!images.isNullOrEmpty()) {
            // TikTok photo mode post: one or many images.
            val items = images.mapIndexedNotNull { index, image ->
                val imageUrl = image.imageURL?.urlList?.firstOrNull() ?: return@mapIndexedNotNull null
                MediaInfoExtraction(
                    type = MediaType.Image,
                    sourceUrl = url,
                    source = name,
                    width = image.imageWidth,
                    height = image.imageHeight,
                    caption = caption,
                    thumbnailUrl = imageUrl,
                    author = authorInfo,
                    download = MediaDownloadableExtraction(
                        contentType = ContentType.Image.JPEG.toString(),
                        url = imageUrl,
                        cookie = cookie,
                        referer = url,
                        filename = getFilenameForMedia(
                            caption, "${uniqueId}_${index + 1}", extension = "jpg",
                        ),
                    ),
                )
            }
            if (items.size == 1) {
                return items[0]
            }
            return MediaInfoExtraction(
                type = MediaType.Carousel,
                sourceUrl = url,
                source = name,
                caption = caption,
                thumbnailUrl = items.firstOrNull()?.thumbnailUrl,
                author = authorInfo,
                items = items,
            )
        }

        return MediaInfoExtraction(
            type = MediaType.Video,
            sourceUrl = url,
            source = name,
            width = video?.width,
            height = video?.height,
            caption = caption,
            duration = video?.duration?.toDouble(),
            thumbnailUrl = video?.cover,
            author = authorInfo,
            download = MediaDownloadableExtraction(
                contentType = ContentType.Video.MP4.toString(),
                url = video?.playAddr ?: "",
                cookie = cookie,
                referer = url,
                filename = getFilenameForMedia(caption, uniqueId)
            ),
        )
    }

    private suspend fun fetchRawData(url: String): Pair<RawData, String> {
        val response = ReelerHttp.client.get(url) {
            headers {
                userAgent(USER_AGENT)
                accept(ContentType.parse(ACCEPT))
                append("Accept-Language", "en-US,en;q=0.9")
            }
        }
        val cookie = response.setCookie().joinToString("; ") { "${it.name}=${it.value}" }
        val html: String = response.body()
        val jsonData = html
            .split("<script id=\"__UNIVERSAL_DATA_FOR_REHYDRATION__\" type=\"application/json\">")
            .getOrNull(1)
            ?.split("</script>")
            ?.getOrNull(0)
            ?: throw ExtractionException("Could not find TikTok media data in the page.")
        val result: RawData = ReelerHttp.json.decodeFromString(jsonData)
        return Pair(result, cookie)
    }
}

private const val BASE_URL = "https://www.tiktok.com"
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
private const val ACCEPT =
    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"

@Serializable
private data class RawData(
    @SerialName("__DEFAULT_SCOPE__")
    val defaultScope: DefaultScope? = null,
)

@Serializable
private data class DefaultScope(
    @SerialName("webapp.video-detail")
    val videoDetail: VideoDetail? = null,
)

@Serializable
private data class VideoDetail(
    val shareMeta: ShareMeta? = null,
    val itemInfo: ItemInfo? = null,
)

@Serializable
private data class ShareMeta(
    val desc: String? = null,
)

@Serializable
private data class ItemInfo(
    val itemStruct: ItemStruct? = null,
)

@Serializable
private data class ItemStruct(
    val desc: String? = null,
    val author: Author? = null,
    val video: Video? = null,
    val imagePost: ImagePost? = null,
)

@Serializable
private data class Author(
    val nickname: String? = null,
    val uniqueId: String? = null,
    val avatarThumb: String? = null,
)

@Serializable
private data class Video(
    val cover: String? = null,
    val playAddr: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
)

@Serializable
private data class ImagePost(
    val images: List<ImagePostImage>? = null,
)

@Serializable
private data class ImagePostImage(
    val imageURL: ImageUrl? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
)

@Serializable
private data class ImageUrl(
    val urlList: List<String>? = null,
)
