package com.catalinalabs.reeler.services

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.live.DownloadStatusHolder
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.schema.files
import com.catalinalabs.reeler.logic.MediaDataFetcher
import com.catalinalabs.reeler.logic.MediaInfoExtractor
import com.catalinalabs.reeler.logic.asMediaInfo
import com.catalinalabs.reeler.logic.downloadables
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DownloadRepository,
    private val notifications: ReelerNotificationsService,
    private val media: ReelerMediaService,
    private val extractor: MediaInfoExtractor,
    private val fetcher: MediaDataFetcher,
    private val status: DownloadStatusHolder,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val sourceUrl = inputData.getString("sourceUrl") ?: return Result.failure()
        try {
            status.processing()
            log("Extracting media info from URL: $sourceUrl")
            val extraction = extractor.extractMediaInfo(sourceUrl)
            val download = repository.create(DownloadLog().apply {
                timestamp = System.currentTimeMillis()
                info = extraction.asMediaInfo()
            })
            log("Inserting download record into database: $download")
            log("Successfully extracted the media info from URL \"$sourceUrl\": $extraction")

            status.downloading()
            for ((index, target) in extraction.downloadables.withIndex()) {
                val data = fetcher.fetchMediaData(target)
                val result = media.writeFileInMediaStore(data, target.filename, target.contentType)
                log("Download of file \"${target.filename}\" completed successfully")
                repository.update(download) {
                    val file = files[index]
                    file.mediaStoreId = result.id
                    file.filePath = result.filePath
                    file.contentLength = data.size.toLong()
                    log("Updated download file info for \"${target.filename}\": $file")
                }
            }

            log("Download of all files completed successfully")
            status.downloadSuccess()
            notifications.showDownloadCompletion(download.timestamp.toInt(), download)
            return Result.success()
        } catch (e: Exception) {
            Log.e(::DownloadWorker.name, "Caught exception in download worker", e)
            e.message?.let { status.error(it) }
            return Result.failure()
        }
    }

    private fun log(message: String) {
        Log.d(::DownloadWorker.name, message)
    }
}
