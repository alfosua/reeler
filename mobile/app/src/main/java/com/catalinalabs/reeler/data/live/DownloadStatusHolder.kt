package com.catalinalabs.reeler.data.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DownloadStatusHolder {
    private val status = MutableLiveData<DownloadStatus>()
    val data: LiveData<DownloadStatus> = status

    fun update(newStatus: DownloadStatus) {
        status.postValue(newStatus)
    }

    fun idle() = update(DownloadStatus.Idle)
    fun processing() = update(DownloadStatus.Processing)
    fun processingSuccess() = update(DownloadStatus.ProcessingSuccess)
    fun downloading() = update(DownloadStatus.Downloading)
    fun downloadSuccess() = update(DownloadStatus.DownloadSuccess)
    fun error(message: String) = update(DownloadStatus.Error(message))
}

interface DownloadStatus {
    object Idle : DownloadStatus
    object Processing : DownloadStatus
    object ProcessingSuccess : DownloadStatus
    object Downloading : DownloadStatus
    object DownloadSuccess : DownloadStatus
    data class Error(
        val message: String,
    ) : DownloadStatus
}
