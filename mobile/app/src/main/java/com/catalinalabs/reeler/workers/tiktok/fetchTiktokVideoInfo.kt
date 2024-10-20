package com.catalinalabs.reeler.workers.tiktok

import com.catalinalabs.reeler.workers.AuthorExtraction
import com.catalinalabs.reeler.workers.MediaDownloadableExtraction
import com.catalinalabs.reeler.workers.MediaInfoExtraction
import com.catalinalabs.reeler.workers.MediaType
import com.catalinalabs.reeler.workers.getFilenameForMedia
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.setCookie
import io.ktor.http.userAgent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun fetchTiktokVideoInfo(url: String): MediaInfoExtraction {
    val (rawData, cookie) = fetchRawData(url)
    val videoDetail = rawData.defaultScope?.videoDetail
    val itemStruct = videoDetail?.itemInfo?.itemStruct
    val video = itemStruct?.video
    val uniqueId = itemStruct?.author?.uniqueId
    val caption = videoDetail?.shareMeta?.desc
        ?.split('\n')?.get(0)

    val result = MediaInfoExtraction(
        type = MediaType.Video,
        sourceUrl = url,
        source = "tiktok",
        width = video?.width,
        height = video?.height,
        caption = caption,
        duration = video?.duration?.toDouble(),
        thumbnailUrl = video?.cover,
        author = AuthorExtraction(
            username = uniqueId,
            avatarUrl = itemStruct?.author?.avatarThumb,
            profileUrl = uniqueId?.let { "$BASE_URL/@${it}/" },
            name = uniqueId?.let { "@${it}" },
            userId = uniqueId,
        ),
        download = MediaDownloadableExtraction(
            contentType = ContentType.Video.MP4.toString(),
            url = video?.playAddr ?: "",
            cookie = cookie,
            referer = url,
            filename = getFilenameForMedia(caption, uniqueId)
        ),
    )

    return result
}

private const val BASE_URL = "https://www.tiktok.com"
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
private const val ACCEPT =
    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"

private val json = Json {
    ignoreUnknownKeys = true
}

private suspend fun fetchRawData(url: String): Pair<RawData, String> {
    val client = HttpClient(CIO)
    val response = client.get(url) {
        headers {
            userAgent(USER_AGENT)
            accept(ContentType.parse(ACCEPT))
            append("Accept-Language", "en-US,en;q=0.9")
        }
    }
    val cookie = response.setCookie().joinToString("; ") { "${it.name}=${it.value}" }
    val html: String = response.body()
    val jsonData = html
        .split("<script id=\"__UNIVERSAL_DATA_FOR_REHYDRATION__\" type=\"application/json\">")[1]
        .split("</script>")[0]
    val result: RawData = json.decodeFromString(jsonData)
    return Pair(result, cookie)
}

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
    val author: Author? = null,
    val video: Video? = null,
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
