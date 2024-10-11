package com.catalinalabs.reeler.workers.instagram

import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.catalinalabs.reeler.workers.getFilenameByTimestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


suspend fun fetchInstagramVideoInfo(url: String): VideoInfoOutput {
    val shortCode = extractShortCodeFromUrl(url)
    val rawData = fetchRawData(shortCode!!)
    val media = rawData.graphql?.shortcodeMedia

    val result = VideoInfoOutput(
        filename = getFilenameByTimestamp(),
        source = "instagram",
        sourceUrl = url,
        width = media?.dimensions?.width,
        height = media?.dimensions?.height,
        contentUrl = media?.videoUrl ?: "",
        caption = media?.edgeMediaToCaption?.edges?.getOrNull(0)?.node?.text,
        duration = media?.videoDuration,
        username = media?.owner?.username,
        userAvatarUrl = media?.owner?.profilePicUrl,
        thumbnailUrl = media?.thumbnailSrc,
    )

    return result
}

private fun extractShortCodeFromUrl(url: String): String? {
    val pattern = Regex(
        pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:instagram\\.com|instagr\\.am)/" +
                "(?:p|reels|tv)/([a-zA-Z0-9_-]+)(?:/.*)?(?:\\?.*)?\$"
    )
    val matchResult = pattern.find(url)
    return matchResult?.groupValues?.getOrNull(1)
}

private const val USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"

private suspend fun fetchRawData(shortCode: String): RawData {
    val url = "https://www.instagram.com/p/$shortCode/?__a=1&__d=dis"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    val response = client.get(url) {
        headers {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            userAgent(USER_AGENT)
        }
    }
    val result: RawData = response.body()
    return result
}

@Serializable
private data class RawData(
    val graphql: Graphql?,
)

@Serializable
private data class Graphql(
    @SerialName("shortcode_media")
    val shortcodeMedia: ShortcodeMedia?,
)

@Serializable
private data class ShortcodeMedia(
    @SerialName("video_url")
    val videoUrl: String?,
    @SerialName("is_video")
    val isVideo: Boolean?,
    @SerialName("display_url")
    val displayUrl: String?,
    val owner: Owner?,
    val dimensions: Dimensions?,
    @SerialName("video_duration")
    val videoDuration: Double?,
    @SerialName("edge_media_to_caption")
    val edgeMediaToCaption: Caption?,
    @SerialName("thumbnail_src")
    val thumbnailSrc: String?,
)

@Serializable
private data class Dimensions(
    val height: Int?,
    val width: Int?,
)

@Serializable
private data class Owner(
    val username: String?,
    @SerialName("profile_pic_url")
    val profilePicUrl: String?,
)

@Serializable
private data class Caption(
    val edges: List<Edge>?,
)

@Serializable
private data class Edge(
    val node: Node?,
)

@Serializable
private data class Node(
    val text: String?,
)
