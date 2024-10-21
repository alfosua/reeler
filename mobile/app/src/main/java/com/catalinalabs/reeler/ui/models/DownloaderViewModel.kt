package com.catalinalabs.reeler.ui.models

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.live.DownloadStatus
import com.catalinalabs.reeler.data.live.DownloadStatusHolder
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.services.DownloadWorker
import com.catalinalabs.reeler.services.ReelerAdsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val statusHolder: DownloadStatusHolder,
    private val ads: ReelerAdsService,
) : ViewModel() {
    val status: LiveData<DownloadStatus> = statusHolder.data
    val download: StateFlow<DownloadLog?> = repository.flowMostRecent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
    var sourceUrl by mutableStateOf("")
        private set
    var alreadyHandledSendAction by mutableStateOf(false)
        private set

    fun startDownloadProcess(context: Context) {
        viewModelScope.launch {
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf("sourceUrl" to sourceUrl))
                .build()
            WorkManager.getInstance(context).enqueue(request)
            ads.showInterstitial(context)
        }
    }

    fun markAsAlreadyHandleSendAction() {
        alreadyHandledSendAction = true
    }

    fun updateSourceUrl(url: String) {
        sourceUrl = url
    }

    fun resetStatus() {
        statusHolder.idle()
    }
}
