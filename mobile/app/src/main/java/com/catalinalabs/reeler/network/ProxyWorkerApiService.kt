package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.utils.RegexExtensions.contains
import com.catalinalabs.reeler.workers.MediaInfoExtraction
import com.catalinalabs.reeler.workers.instagram.fetchInstagramVideoInfo
import com.catalinalabs.reeler.workers.tiktok.fetchTiktokVideoInfo
import com.catalinalabs.reeler.workers.twitter.fetchTwitterVideoInfo
import com.catalinalabs.reeler.workers.youtube.fetchYoutubeVideoInfo
import io.ktor.http.Url

class ProxyWorkerApiService : WorkerApiService {
    override suspend fun getVideoInfo(sourceUrl: String): MediaInfoExtraction {
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
                val workerApi = KtorWorkerApiService()
                workerApi::getVideoInfo
            }
        }
        val result = fetch(sourceUrl)
        return result
    }
}