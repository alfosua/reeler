package com.catalinalabs.reeler.logic

import com.catalinalabs.reeler.logic.instagram.fetchInstagramVideoInfo
import com.catalinalabs.reeler.logic.tiktok.fetchTiktokVideoInfo
import com.catalinalabs.reeler.logic.twitter.fetchTwitterVideoInfo
import com.catalinalabs.reeler.logic.youtube.fetchYoutubeVideoInfo
import com.catalinalabs.reeler.utils.RegexExtensions.contains
import io.ktor.http.Url

class MediaInfoExtractor {
    suspend fun extractMediaInfo(sourceUrl: String): MediaInfoExtraction {
        val url = Url(sourceUrl)
        val fetch = when (url.host) {
            "www.youtube.com", "m.youtube.com", "youtube.com", "youtu.be" -> {
                ::fetchYoutubeVideoInfo
            }

            in Regex("(?:www\\.|vm\\.)?tiktok\\.com") -> {
                ::fetchTiktokVideoInfo
            }

            in Regex("(?:www\\.|m\\.)?(?:instagram\\.com|instagr\\.am)") -> {
                ::fetchInstagramVideoInfo
            }

            in Regex("(?:www\\.|m\\.|)?(?:twitter\\.com|x\\.com)") -> {
                ::fetchTwitterVideoInfo
            }

            else -> {
                throw NotImplementedError("Unsupported source URL: $sourceUrl")
            }
        }
        val result = fetch(sourceUrl)
        return result
    }
}