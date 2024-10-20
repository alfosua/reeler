package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.workers.MediaDownloadableExtraction

interface VideoDataFetcher {
    suspend fun getVideoData(info: MediaDownloadableExtraction): ByteArray

    companion object {
        const val LOG_TAG = "VideoDataFetcher"
    }
}

