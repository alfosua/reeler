package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.workers.MediaInfoExtraction

interface WorkerApiService {
    suspend fun getVideoInfo(sourceUrl: String): MediaInfoExtraction

    companion object {
        const val LOG_TAG = "WorkerApiService"
    }
}

