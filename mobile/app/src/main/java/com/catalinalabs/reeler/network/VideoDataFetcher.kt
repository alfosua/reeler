package com.catalinalabs.reeler.network

interface VideoDataFetcher {
    suspend fun getVideoData(contentUrl: String): ByteArray

    companion object {
        const val LOG_TAG = "VideoDataFetcher"
    }
}

