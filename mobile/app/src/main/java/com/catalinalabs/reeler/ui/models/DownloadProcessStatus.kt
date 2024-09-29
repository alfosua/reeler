package com.catalinalabs.reeler.ui.models

import com.catalinalabs.reeler.network.models.VideoInfoOutput

interface DownloadProcessStatus {
    object Idle : DownloadProcessStatus
    object Processing : DownloadProcessStatus
    object ProcessingSuccess : DownloadProcessStatus
    object Downloading : DownloadProcessStatus
    object DownloadSuccess : DownloadProcessStatus
    data class Error(
        val message: String,
        val whenHappened: String = "processing"
    ) : DownloadProcessStatus
}

