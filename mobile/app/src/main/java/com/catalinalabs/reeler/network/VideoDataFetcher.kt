package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.network.models.VideoInfoOutput

interface VideoDataFetcher {
    suspend fun getVideoData(info: VideoInfoOutput): ByteArray

    companion object {
        const val LOG_TAG = "VideoDataFetcher"
    }
}

