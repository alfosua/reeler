package com.catalinalabs.reeler.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class KtorVideoDataFetcher: VideoDataFetcher {
    override suspend fun getVideoData(contentUrl: String): ByteArray {
        Log.d(VideoDataFetcher.LOG_TAG, "Requesting video data at: $contentUrl")
        val client = HttpClient(CIO)
        val response: HttpResponse = client.get(contentUrl)
        val responseData: ByteArray = response.body()
        return responseData
    }
}