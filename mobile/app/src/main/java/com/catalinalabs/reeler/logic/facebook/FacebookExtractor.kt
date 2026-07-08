package com.catalinalabs.reeler.logic.facebook

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

/**
 * Facebook videos, reels and watch pages. Ported from yt-dlp's FacebookIE:
 * the watch page embeds JSON with browser_native_hd_url/playable_url fields
 * that point at the raw MP4.
 */
object FacebookExtractor : Extractor {
    override val name = "facebook"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:www\\.|m\\.|web\\.)?(?:facebook\\.com|fb\\.watch|fb\\.com)/"
    )

    override suspend fun extract(url: String): MediaInfoExtraction {
        val html = fetchPage(url)

        val videoUrl = findJsonString(html, "browser_native_hd_url")
            ?: findJsonString(html, "browser_native_sd_url")
            ?: findJsonString(html, "playable_url_quality_hd")
            ?: findJsonString(html, "playable_url")
            ?: throw ExtractionException(
                "Could not find a video on this Facebook page; it may be private or require login."
            )

        val title = findMetaContent(html, "og:title")
            ?: findJsonString(html, "name")
        val caption = title?.split('\n')?.get(0)
        val thumbnail = findMetaContent(html, "og:image")
        val ownerName = Regex("\"owner\"\\s*:\\s*\\{[^{}]*\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .find(html)?.groupValues?.getOrNull(1)?.let { unescapeJsonString(it) }
        val videoId = Regex("\"video_id\"\\s*:\\s*\"(\\d+)\"").find(html)
            ?.groupValues?.getOrNull(1)
            ?: Regex("/(?:videos|reel)/(\\d+)").find(url)?.groupValues?.getOrNull(1)

        return MediaInfoExtraction(
            type = MediaType.Video,
            sourceUrl = url,
            source = name,
            caption = caption,
            thumbnailUrl = thumbnail,
            author = AuthorExtraction(
                name = ownerName,
                username = ownerName,
                userId = ownerName,
            ),
            download = MediaDownloadableExtraction(
                contentType = ContentType.Video.MP4.toString(),
                url = videoUrl,
                referer = "https://www.facebook.com/",
                filename = getFilenameForMedia(caption, videoId),
            ),
        )
    }

    private suspend fun fetchPage(url: String): String {
        val response = ReelerHttp.client.get(url) {
            headers {
                userAgent(ReelerHttp.DEFAULT_USER_AGENT)
                append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                append("Accept-Language", "en-US,en;q=0.9")
                append("Sec-Fetch-Mode", "navigate")
            }
        }
        return response.body()
    }

    /**
     * Finds `"key":"value"` in the raw page JSON and decodes the escaped
     * value ("\/" and "\uXXXX" sequences).
     */
    private fun findJsonString(html: String, key: String): String? {
        val match = Regex("\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(html)
            ?: return null
        val raw = match.groupValues[1]
        if (raw.isEmpty()) return null
        return unescapeJsonString(raw)
    }

    private fun findMetaContent(html: String, property: String): String? {
        val pattern = Regex(
            "<meta[^>]+property=[\"']$property[\"'][^>]+content=[\"']([^\"']+)[\"']"
        )
        return pattern.find(html)?.groupValues?.getOrNull(1)
            ?.replace("&amp;", "&")
    }

    private fun unescapeJsonString(raw: String): String {
        val builder = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val char = raw[i]
            if (char == '\\' && i + 1 < raw.length) {
                when (val next = raw[i + 1]) {
                    'u' -> {
                        if (i + 5 < raw.length) {
                            val code = raw.substring(i + 2, i + 6).toIntOrNull(16)
                            if (code != null) {
                                builder.append(code.toChar())
                                i += 6
                                continue
                            }
                        }
                        builder.append(next)
                        i += 2
                    }

                    'n' -> {
                        builder.append('\n'); i += 2
                    }

                    't' -> {
                        builder.append('\t'); i += 2
                    }

                    else -> {
                        builder.append(next); i += 2
                    }
                }
            } else {
                builder.append(char)
                i++
            }
        }
        return builder.toString()
    }
}
