package com.catalinalabs.reeler.network

import com.catalinalabs.reeler.network.models.VideoInfoOutput

interface WorkerApiService {
    suspend fun getVideo(): VideoInfoOutput
}
