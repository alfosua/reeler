package com.catalinalabs.reeler.network

import android.util.Log
import com.catalinalabs.reeler.utils.RegexExtensions.contains
import com.catalinalabs.reeler.workers.MediaDownloadableExtraction
import com.catalinalabs.reeler.workers.tiktok.fetchTiktokVideoData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url

class ProxyVideoDataFetcher : VideoDataFetcher {
    override suspend fun getVideoData(info: MediaDownloadableExtraction): ByteArray {
        Log.d(VideoDataFetcher.LOG_TAG, "Requesting video data at: ${info.url}")
        val url = Url(info.url)
        val fetch = when (url.host) {
            in Regex("(?:.*\\.)?tiktok\\.com") -> {
                ::fetchTiktokVideoData
            }

            else -> {
                val fetcher = KtorVideoDataFetcher()
                fetcher::getVideoData
            }
        }
        val result = fetch(info)
        return result
    }
}

class KtorVideoDataFetcher : VideoDataFetcher {
    override suspend fun getVideoData(info: MediaDownloadableExtraction): ByteArray {
        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 3600000
            }
        }
        val response: HttpResponse = client.get(info.url)
        val responseData: ByteArray = response.body()
        return responseData
    }
}
