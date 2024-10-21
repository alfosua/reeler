package com.catalinalabs.reeler.data.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DownloadStatusHolder {
    private val status = MutableLiveData<DownloadStatus>()
    val data: LiveData<DownloadStatus> = status

    private fun update(newStatus: DownloadStatus) {
        status.postValue(newStatus)
    }

    fun idle() = update(DownloadStatus.Idle)

    fun processing() = update(DownloadStatus.Processing)

    fun downloading(
        progress: Double? = null,
        index: Int? = null,
        count: Int? = null,
    ) = update(DownloadStatus.Downloading(progress, index, count))

    fun downloadSuccess() = update(DownloadStatus.Success)

    fun error(message: String) = update(DownloadStatus.Error(message))
}

interface DownloadStatus {
    object Idle : DownloadStatus

    object Processing : DownloadStatus

    data class Downloading(
        val progress: Double? = null,
        val index: Int? = null,
        val count: Int? = null,
    ) : DownloadStatus

    object Success : DownloadStatus

    data class Error(
        val message: String,
    ) : DownloadStatus
}
