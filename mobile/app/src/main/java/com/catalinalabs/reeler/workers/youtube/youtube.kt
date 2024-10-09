package com.catalinalabs.reeler.workers.youtube

import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun fetchYoutubeVideoInfo(url: String): VideoInfoOutput {
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

    val result = VideoInfoOutput(
        filename = getFilenameByTimestamp(),
        sourceUrl = url,
        source = "youtube",
        contentUrl = format.url(),
        caption = details.title(),
        duration = details.lengthSeconds().toDouble(),
        username = details.author(),
        thumbnailUrl = details.thumbnails().firstOrNull(),
        width = format.width(),
        height = format.height(),
        userAvatarUrl = authorAvatarUrl,
    )

    return result
}

private fun extractVideoIdFromYoutubeUrl(url: String): String? {
    val pattern = Regex(
        pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/" +
                "(?:watch\\?v=|embed/|v/|shorts/)?([a-zA-Z0-9_-]{11})(?:&.+)?\$"
    )
    val matchResult = pattern.find(url)
    return matchResult?.groupValues?.getOrNull(1)
}

private fun getFilenameByTimestamp(extension: String = "mp4"): String {
    val timestamp = System.currentTimeMillis()
    return "video_$timestamp.$extension"
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
