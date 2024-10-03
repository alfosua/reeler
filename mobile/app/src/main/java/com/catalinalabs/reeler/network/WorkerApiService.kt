package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.network.models.VideoInfoOutput

interface WorkerApiService {
    suspend fun getVideoInfo(sourceUrl: String): VideoInfoOutput
    companion object {
        const val LOG_TAG = "WorkerApiService"
    }
}

