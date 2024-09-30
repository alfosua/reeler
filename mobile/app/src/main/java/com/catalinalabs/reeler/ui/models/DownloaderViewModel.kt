package com.catalinalabs.reeler.ui.models

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {
    var videoInfo: VideoInfoOutput? by mutableStateOf(null)
        private set
    var sourceUrl by mutableStateOf("")
        private set
    var status: DownloadProcessStatus by mutableStateOf(DownloadProcessStatus.Idle)
        private set

    fun processVideoInfo() {
        viewModelScope.launch {
            status = DownloadProcessStatus.Processing
            status = try {
                Log.d(::DownloaderViewModel.name, "Processing video info for URL: $sourceUrl")
                videoInfo = fetchVideoInfo(sourceUrl)
                Log.d(::DownloaderViewModel.name, "Successfully processed video info from URL \"$sourceUrl\": $videoInfo")
                DownloadProcessStatus.ProcessingSuccess
            } catch (e: Exception) {
                Log.e(::DownloaderViewModel.name, "Failed to process video info: $e")
                DownloadProcessStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun processDownload(saveFileAction: (ByteArray, VideoInfoOutput) -> String) {
        viewModelScope.launch {
            val videoInfo = videoInfo
            status = DownloadProcessStatus.Downloading
            status = try {
                if (videoInfo == null) {
                    throw Exception("No video info available")
                }
                Log.d(::DownloaderViewModel.name, "Starting download of video")
                val data: ByteArray = fetchVideoData(videoInfo)
                val mediaUri = saveFileAction(data, videoInfo)
                Log.d(::DownloaderViewModel.name, "Download of video \"${videoInfo.filename}\" completed successfully")
                saveVideoDataIntoDatabase(videoInfo, mediaUri)
                DownloadProcessStatus.DownloadSuccess
            } catch (e: Exception) {
                Log.e(::DownloaderViewModel.name, "Failed to process download: $e")
                DownloadProcessStatus.Error(e.message ?: "Unknown error", "downloading")
            }
            sourceUrl = ""
        }
    }

    @JvmName("setVideoUrlPublic")
    fun setVideoUrl(url: String) {
        sourceUrl = url
    }

    private suspend fun fetchVideoInfo(sourceUrl: String): VideoInfoOutput {
        val targetUrl = "https://instagram.alfosuag.workers.dev/video-info?url=$sourceUrl"
        Log.d(::DownloaderViewModel.name, "Requesting video information at: $targetUrl")
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val response: HttpResponse = client.get(targetUrl)
        val videoInfo: VideoInfoOutput = response.body()
        return videoInfo
    }

    private suspend inline fun<reified T> fetchVideoData(videoInfo: VideoInfoOutput): T {
        Log.d(::DownloaderViewModel.name, "Requesting video data at: $videoInfo.contentUrl")
        val client = HttpClient(CIO)
        val response: HttpResponse = client.get(videoInfo.contentUrl)
        val responseData: T = response.body()
        return responseData
    }

    private suspend fun saveVideoDataIntoDatabase(videoInfo: VideoInfoOutput, mediaUri: String?) {
        val date = Calendar.getInstance().time
        val entity = videoInfo.asEntity(
            mediaUri = mediaUri,
            date = date.toString(),
        )
        Log.d(::DownloaderViewModel.name, "Inserting download record into database: $entity")
        repository.insertDownload(entity)
    }
}

private fun VideoInfoOutput.asEntity(
    id: Int = 0,
    mediaUri: String? = null,
    date: String? = null,
): DownloadEntity {
    return DownloadEntity(
        id = id,
        mediaUri = mediaUri,
        date = date,
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
    )
}
