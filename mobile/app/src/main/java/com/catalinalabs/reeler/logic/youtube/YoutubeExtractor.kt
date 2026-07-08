package com.catalinalabs.reeler.logic.youtube

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.core.ExtractionException
import com.catalinalabs.reeler.logic.core.Extractor
import com.catalinalabs.reeler.logic.core.ReelerHttp
import com.catalinalabs.reeler.logic.getFilenameForMedia
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

object YoutubeExtractor : Extractor {
    override val name = "youtube"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/"
    )

    private val downloader by lazy { YoutubeDownloader() }

    override suspend fun extract(url: String): MediaInfoExtraction {
        val raw = fetchRawData(url)

        val authorAvatarUrl = raw.endscreen?.endscreenRenderer?.elements
            ?.find { it.endscreenElementRenderer.style == "CHANNEL" }
            ?.endscreenElementRenderer?.image?.thumbnails?.getOrNull(0)?.url

        val videoId = extractVideoIdFromYoutubeUrl(url)
        val video = withContext(Dispatchers.IO) {
            downloader.getVideoInfo(RequestVideoInfo(videoId)).data()
        } ?: throw ExtractionException(
            "Could not load this YouTube video; it may be private, age-restricted or region-locked."
        )
        val details = video.details()
        val format = video.bestVideoWithAudioFormat()
        val caption = details.title()

        val result = MediaInfoExtraction(
            type = MediaType.Video,
            sourceUrl = url,
            source = name,
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

    private fun extractVideoIdFromYoutubeUrl(url: String): String? {
        val pattern = Regex(
            pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com|youtu\\.be)/" +
                    "(?:watch\\?v=|embed/|v/|shorts/)?([a-zA-Z0-9_-]{11})(?:&.+|\\?.+)?\$"
        )
        val matchResult = pattern.find(url)
        return matchResult?.groupValues?.getOrNull(1)
    }

    private suspend fun fetchRawData(url: String): PlayerResponse {
        val response: String = ReelerHttp.client.get(url).body()
        val rawDataString = response
            .split("var ytInitialPlayerResponse = ")
            .getOrNull(1)
            ?.split(";</script>")
            ?.getOrNull(0)

        val playerResponse = rawDataString?.let {
            ReelerHttp.json.decodeFromString<PlayerResponse>(it)
        } ?: PlayerResponse()

        return playerResponse
    }
}

const val BASE_URL = "https://www.youtube.com"

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
    val endscreenRenderer: EndscreenRenderer? = null,
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
