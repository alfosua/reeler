package com.catalinalabs.reeler.ui.models

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.network.VideoDataFetcher
import com.catalinalabs.reeler.network.WorkerApiService
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.catalinalabs.reeler.services.ReelerAdsService
import com.catalinalabs.reeler.services.ReelerMediaService
import com.catalinalabs.reeler.services.ReelerNotificationsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
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
    var download: DownloadEntity? by mutableStateOf(null)
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

    fun updateDownload(newDownload: DownloadEntity?) {
        download = newDownload
    }

    fun updateStatus(newStatus: DownloadProcessStatus) {
        status = newStatus
    }

    private suspend fun workOnVideoProcessing(): VideoInfoOutput? {
        try {
            updateStatus(DownloadProcessStatus.Processing)
            Log.d(::DownloaderViewModel.name, "Processing video info for URL: $sourceUrl")
            val videoInfo = workerApi.getVideoInfo(sourceUrl)
            updateDownload(videoInfo.asEntity())
            Log.d(
                ::DownloaderViewModel.name,
                "Successfully processed video info from URL \"$sourceUrl\": $videoInfo"
            )
            updateStatus(DownloadProcessStatus.ProcessingSuccess)
            updateSourceUrl("")
            return videoInfo
        } catch (e: Exception) {
            Log.e(::DownloaderViewModel.name, "Failed to process video info: $e")
            updateStatus(DownloadProcessStatus.Error(e.message ?: "Unknown error", "processing"))
            return null
        }
    }

    private suspend fun workOnVideoDownload(videoInfo: VideoInfoOutput) {
        try {
            updateStatus(DownloadProcessStatus.Downloading)
            Log.d(::DownloaderViewModel.name, "Starting download of video")

            val data = videoFetcher.getVideoData(videoInfo)
            val filePath = media.saveVideo(data, videoInfo)
            Log.d(
                ::DownloaderViewModel.name,
                "Download of video \"${videoInfo.filename}\" completed successfully"
            )

            val timestamp = Calendar.getInstance().time.time
            val download =
                saveVideoDataIntoDatabase(videoInfo, filePath, timestamp, data.size.toLong())

            updateDownload(download)
            notifications.showDownloadCompletion(timestamp.toInt(), download)
            updateStatus(DownloadProcessStatus.DownloadSuccess)
        } catch (e: Exception) {
            Log.e(::DownloaderViewModel.name, "Failed to process download: $e")
            updateStatus(DownloadProcessStatus.Error(e.message ?: "Unknown error", "downloading"))
        }
        sourceUrl = ""
    }

    private suspend fun saveVideoDataIntoDatabase(
        videoInfo: VideoInfoOutput,
        filePath: String?,
        timestamp: Long?,
        size: Long,
    ): DownloadEntity {
        val entity = videoInfo.asEntity(
            filePath = filePath,
            timestamp = timestamp,
            size = size,
        )
        Log.d(::DownloaderViewModel.name, "Inserting download record into database: $entity")
        val id = repository.insertDownload(entity)
        val newEntity = repository.getDownloadStream(id.toInt()).first()
        return newEntity!!
    }
}

private fun VideoInfoOutput.asEntity(
    id: Long = 0,
    filePath: String? = null,
    timestamp: Long? = null,
    size: Long = 0,
): DownloadEntity {
    return DownloadEntity(
        id = id,
        filePath = filePath,
        timestamp = timestamp,
        filename = this.filename,
        contentUrl = this.contentUrl,
        sourceUrl = this.sourceUrl,
        source = this.source,
        width = this.width,
        height = this.height,
        username = this.username,
        caption = this.caption,
        duration = this.duration,
        userAvatarUrl = this.userAvatarUrl,
        thumbnailUrl = this.thumbnailUrl,
        size = size,
    )
}
