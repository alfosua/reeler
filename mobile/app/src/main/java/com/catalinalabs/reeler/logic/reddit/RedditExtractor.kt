package com.catalinalabs.reeler.logic.reddit

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
import io.ktor.http.userAgent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Reddit posts: videos (v.redd.it), single images (i.redd.it) and galleries.
 * Ported from yt-dlp's RedditIE approach of appending .json to the post URL.
 */
object RedditExtractor : Extractor {
    override val name = "reddit"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:www\\.|old\\.|new\\.|np\\.)?(?:reddit\\.com|redd\\.it)/"
    )

    override suspend fun extract(url: String): MediaInfoExtraction {
        val post = fetchPostData(url)
        val caption = post.title?.split('\n')?.get(0)
        val author = AuthorExtraction(
            username = post.author,
            profileUrl = post.author?.let { "https://www.reddit.com/user/$it/" },
            name = post.author?.let { "u/$it" },
            userId = post.author,
        )
        val sourceId = post.id ?: "reddit"
        val thumbnail = post.preview?.images?.firstOrNull()?.source?.url?.unescapeHtml()
            ?: post.thumbnail?.takeIf { it.startsWith("http") }

        // Video post
        val video = post.media?.redditVideo ?: post.secureMedia?.redditVideo
        if (video?.fallbackUrl != null) {
            return MediaInfoExtraction(
                type = MediaType.Video,
                sourceUrl = url,
                source = name,
                width = video.width,
                height = video.height,
                caption = caption,
                duration = video.duration?.toDouble(),
                thumbnailUrl = thumbnail,
                author = author,
                download = MediaDownloadableExtraction(
                    contentType = ContentType.Video.MP4.toString(),
                    url = video.fallbackUrl,
                    referer = "https://www.reddit.com/",
                    filename = getFilenameForMedia(caption, sourceId),
                ),
            )
        }

        // Gallery post
        val galleryItems = post.galleryData?.items
        if (!galleryItems.isNullOrEmpty() && post.mediaMetadata != null) {
            val items = galleryItems.mapIndexedNotNull { index, item ->
                val mediaId = item.mediaId ?: return@mapIndexedNotNull null
                val meta = post.mediaMetadata[mediaId] ?: return@mapIndexedNotNull null
                val mime = meta.m ?: "image/jpeg"
                val ext = mime.substringAfterLast('/').replace("jpeg", "jpg")
                val imageUrl = meta.s?.u?.unescapeHtml()
                    ?: meta.s?.gif
                    ?: return@mapIndexedNotNull null
                MediaInfoExtraction(
                    type = MediaType.Image,
                    sourceUrl = url,
                    source = name,
                    width = meta.s?.x,
                    height = meta.s?.y,
                    caption = caption,
                    thumbnailUrl = imageUrl,
                    author = author,
                    download = MediaDownloadableExtraction(
                        contentType = mime,
                        url = imageUrl,
                        filename = getFilenameForMedia(
                            caption, "${sourceId}_${index + 1}", extension = ext,
                        ),
                    ),
                )
            }
            if (items.size == 1) return items[0]
            if (items.isNotEmpty()) {
                return MediaInfoExtraction(
                    type = MediaType.Carousel,
                    sourceUrl = url,
                    source = name,
                    caption = caption,
                    thumbnailUrl = items.firstOrNull()?.thumbnailUrl,
                    author = author,
                    items = items,
                )
            }
        }

        // Direct image post
        val direct = post.url?.unescapeHtml()
        if (direct != null && Regex("\\.(jpe?g|png|gif|webp)(\\?.*)?$").containsMatchIn(direct)) {
            val ext = direct.substringAfterLast('.').substringBefore('?')
                .replace("jpeg", "jpg")
            val mime = when (ext) {
                "png" -> ContentType.Image.PNG.toString()
                "gif" -> ContentType.Image.GIF.toString()
                "webp" -> "image/webp"
                else -> ContentType.Image.JPEG.toString()
            }
            return MediaInfoExtraction(
                type = MediaType.Image,
                sourceUrl = url,
                source = name,
                caption = caption,
                thumbnailUrl = direct,
                author = author,
                download = MediaDownloadableExtraction(
                    contentType = mime,
                    url = direct,
                    filename = getFilenameForMedia(caption, sourceId, extension = ext),
                ),
            )
        }

        throw ExtractionException("This Reddit post has no downloadable video or image.")
    }

    private suspend fun fetchPostData(url: String): PostData {
        val cleanUrl = url
            .substringBefore('?')
            .trimEnd('/')
        val response = ReelerHttp.client.get("$cleanUrl.json?raw_json=1") {
            headers {
                userAgent(ReelerHttp.DEFAULT_USER_AGENT)
            }
        }
        val body: String = response.body()
        val root = ReelerHttp.json.parseToJsonElement(body)
        val postJson = try {
            root.jsonArray[0].jsonObject["data"]!!
                .jsonObject["children"]!!
                .jsonArray[0].jsonObject["data"]!!.jsonObject
        } catch (e: Exception) {
            throw ExtractionException("Could not read Reddit post data.", e)
        }
        return ReelerHttp.json.decodeFromJsonElement(PostData.serializer(), postJson)
    }

    private fun String.unescapeHtml(): String = this.replace("&amp;", "&")
}

@Serializable
private data class PostData(
    val id: String? = null,
    val title: String? = null,
    val author: String? = null,
    val url: String? = null,
    val thumbnail: String? = null,
    val media: MediaField? = null,
    @SerialName("secure_media")
    val secureMedia: MediaField? = null,
    val preview: Preview? = null,
    @SerialName("gallery_data")
    val galleryData: GalleryData? = null,
    @SerialName("media_metadata")
    val mediaMetadata: Map<String, MediaMetadata>? = null,
)

@Serializable
private data class MediaField(
    @SerialName("reddit_video")
    val redditVideo: RedditVideo? = null,
)

@Serializable
private data class RedditVideo(
    @SerialName("fallback_url")
    val fallbackUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
)

@Serializable
private data class Preview(
    val images: List<PreviewImage>? = null,
)

@Serializable
private data class PreviewImage(
    val source: PreviewSource? = null,
)

@Serializable
private data class PreviewSource(
    val url: String? = null,
)

@Serializable
private data class GalleryData(
    val items: List<GalleryItem>? = null,
)

@Serializable
private data class GalleryItem(
    @SerialName("media_id")
    val mediaId: String? = null,
)

@Serializable
private data class MediaMetadata(
    val m: String? = null,
    val s: MetadataSource? = null,
)

@Serializable
private data class MetadataSource(
    val u: String? = null,
    val gif: String? = null,
    val x: Int? = null,
    val y: Int? = null,
)
