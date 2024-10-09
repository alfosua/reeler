package com.catalinalabs.reeler.network

import android.util.Log
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorWorkerApiService : WorkerApiService {
    override suspend fun getVideoInfo(sourceUrl: String): VideoInfoOutput {
        val targetUrl = "https://instagram.alfosuag.workers.dev/video-info?url=$sourceUrl"
        Log.d(WorkerApiService.LOG_TAG, "Requesting video information at: $targetUrl")
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val response = client.get(targetUrl)
        val videoInfo: VideoInfoOutput = response.body()
        return videoInfo
    }
}


