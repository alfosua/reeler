package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.catalinalabs.reeler.utils.RegexExtensions.contains
import com.catalinalabs.reeler.workers.instagram.fetchInstagramVideoInfo
import com.catalinalabs.reeler.workers.tiktok.fetchTiktokVideoInfo
import com.catalinalabs.reeler.workers.youtube.fetchYoutubeVideoInfo
import io.ktor.http.Url

class ProxyWorkerApiService : WorkerApiService {
    override suspend fun getVideoInfo(sourceUrl: String): VideoInfoOutput {
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

            else -> {
                val workerApi = KtorWorkerApiService()
                workerApi::getVideoInfo
            }
        }
        val result = fetch(sourceUrl)
        return result
    }
}