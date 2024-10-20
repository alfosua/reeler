package com.catalinalabs.reeler.ui.models

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.schema.files
import com.catalinalabs.reeler.network.VideoDataFetcher
import com.catalinalabs.reeler.network.WorkerApiService
import com.catalinalabs.reeler.services.ReelerAdsService
import com.catalinalabs.reeler.services.ReelerMediaService
import com.catalinalabs.reeler.services.ReelerNotificationsService
import com.catalinalabs.reeler.workers.MediaInfoExtraction
import com.catalinalabs.reeler.workers.asMediaInfo
import com.catalinalabs.reeler.workers.downloadables
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val notifications: ReelerNotificationsService,
    private val media: ReelerMediaService,
    private val workerApi: WorkerApiService,
    private val videoFetcher: VideoDataFetcher,
    private val ads: ReelerAdsService,
) : ViewModel() {
    var download: DownloadLog? by mutableStateOf(null)
        private set
    var sourceUrl by mutableStateOf("")
        private set
    var alreadyHandledSendAction by mutableStateOf(false)
        private set
    var status: DownloadProcessStatus by mutableStateOf(DownloadProcessStatus.Idle)
        private set

    fun startDownloadProcess(context: Context) {
        viewModelScope.launch {
            val videoInfo = workOnVideoProcessing()
            if (videoInfo != null) {
                ads.showInterstitial(context)
                workOnVideoDownload(videoInfo)
            }
        }
    }

    fun markAsAlreadyHandleSendAction() {
        alreadyHandledSendAction = true
    }

    fun updateSourceUrl(url: String) {
        sourceUrl = url
    }

    fun updateDownload(newDownload: DownloadLog?) {
        download = newDownload
    }

    fun updateStatus(newStatus: DownloadProcessStatus) {
        status = newStatus
    }

    private suspend fun workOnVideoProcessing(): MediaInfoExtraction? {
        try {
            updateStatus(DownloadProcessStatus.Processing)
            Log.d(::DownloaderViewModel.name, "Processing video info for URL: $sourceUrl")
            val extraction = workerApi.getVideoInfo(sourceUrl)
            val download = createDownloadLog(extraction)
            updateDownload(download)
            Log.d(
                ::DownloaderViewModel.name,
                "Successfully processed video info from URL \"$sourceUrl\": $extraction"
            )
            updateStatus(DownloadProcessStatus.ProcessingSuccess)
            updateSourceUrl("")
            return extraction
        } catch (e: Exception) {
            Log.e(::DownloaderViewModel.name, "Failed to process video info: $e")
            updateStatus(DownloadProcessStatus.Error(e.message ?: "Unknown error", "processing"))
            return null
        }
    }

    private suspend fun workOnVideoDownload(extraction: MediaInfoExtraction) {
        try {
            val download = download ?: throw Exception("Download log is null")

            updateStatus(DownloadProcessStatus.Downloading)
            Log.d(::DownloaderViewModel.name, "Starting download of video")

            var updatedDownload: DownloadLog? = null
            for ((index, target) in extraction.downloadables.withIndex()) {
                val data = videoFetcher.getVideoData(target)
                val result = media.writeFileInMediaStore(data, target.filename, target.contentType)
                Log.d(
                    ::DownloaderViewModel.name,
                    "Download of video \"${target.filename}\" completed successfully"
                )
                updatedDownload = repository.update(download) {
                    val file = files[index]
                    file.mediaStoreId = result.id
                    file.filePath = result.filePath
                    file.contentLength = data.size.toLong()
                    Log.d(
                        ::DownloaderViewModel.name,
                        "Updated download file info for \"${target.filename}\": $file"
                    )
                }
            }

            if (updatedDownload != null) {
                updateDownload(updatedDownload)
            }
            notifications.showDownloadCompletion(download.timestamp.toInt(), download)
            updateStatus(DownloadProcessStatus.DownloadSuccess)
        } catch (e: Exception) {
            Log.e(::DownloaderViewModel.name, "Failed to process download: $e")
            updateStatus(DownloadProcessStatus.Error(e.message ?: "Unknown error", "downloading"))
        }
        updateSourceUrl("")
    }

    private suspend fun createDownloadLog(extraction: MediaInfoExtraction): DownloadLog {
        val download = DownloadLog().apply {
            timestamp = Calendar.getInstance().time.time
            info = extraction.asMediaInfo()
        }
        Log.d(::DownloaderViewModel.name, "Inserting download record into database: $download")
        return repository.create(download)
    }
}
