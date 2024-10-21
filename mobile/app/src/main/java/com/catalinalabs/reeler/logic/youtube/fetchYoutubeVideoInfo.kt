package com.catalinalabs.reeler.logic.youtube

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.getFilenameForMedia
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun fetchYoutubeVideoInfo(url: String): MediaInfoExtraction {
    val raw = fetchRawData(url)

    val authorAvatarUrl = raw.endscreen?.endscreenRenderer?.elements
        ?.find { it.endscreenElementRenderer.style == "CHANNEL" }
        ?.endscreenElementRenderer?.image?.thumbnails?.getOrNull(0)?.url

    val downloader = YoutubeDownloader()

    val videoId = extractVideoIdFromYoutubeUrl(url)
    val video = withContext(Dispatchers.IO) {
        downloader.getVideoInfo(RequestVideoInfo(videoId)).data()
    }
    val details = video.details()
    val format = video.bestVideoWithAudioFormat()
    val caption = details.title()

    val result = MediaInfoExtraction(
        type = MediaType.Video,
        sourceUrl = url,
        source = "youtube",
        width = format.width(),
        height = format.height(),
        caption = caption,
        duration = details.lengthSeconds().toDouble(),
        thumbnailUrl = details.thumbnails().firstOrNull(),
        author = AuthorExtraction(
            username = details.author(),
            avatarUrl = authorAvatarUrl,
            profileUrl = details.author()?.let { "$BASE_URL/@${it}/" },
            name = details.author(),
            userId = details.author(),
        ),
        download = MediaDownloadableExtraction(
            contentType = ContentType.Video.MP4.toString(),
            url = format.url(),
            filename = getFilenameForMedia(caption, videoId),
        ),
    )

    return result
}

const val BASE_URL = "https://www.youtube.com"

private fun extractVideoIdFromYoutubeUrl(url: String): String? {
    val pattern = Regex(
        pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/" +
                "(?:watch\\?v=|embed/|v/|shorts/)?([a-zA-Z0-9_-]{11})(?:&.+|\\?.+)?\$"
    )
    val matchResult = pattern.find(url)
    return matchResult?.groupValues?.getOrNull(1)
}

private val json = Json {
    ignoreUnknownKeys = true
}

private suspend fun fetchRawData(url: String): PlayerResponse {
    val client = HttpClient(CIO) // or your existing Ktor client instance
    val response: String = client.get(url).body()
    val rawDataString = response
        .split("var ytInitialPlayerResponse = ")
        .getOrNull(1) // Handle potential index out of bounds
        ?.split(";</script>")
        ?.getOrNull(0) // Handle potential index out of bounds

    val playerResponse = rawDataString?.let { json.decodeFromString<PlayerResponse>(it) }
        ?: throw Exception("Could not extract Player Response")

    return playerResponse
}

@Serializable
data class PlayerResponse(
    val endscreen: Endscreen? = null,
)

@Serializable
data class ThumbnailItem(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class Endscreen(
    val endscreenRenderer: EndscreenRenderer? = null, // Optional property
)

@Serializable
data class EndscreenRenderer(
    val elements: List<Element>,
)

@Serializable
data class Element(
    val endscreenElementRenderer: EndscreenElementRenderer,
)

@Serializable
data class EndscreenElementRenderer(
    val style: String,
    val image: Image,
)

@Serializable
data class Image(
    val thumbnails: List<ThumbnailItem>,
)
