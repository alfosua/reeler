package com.catalinalabs.reeler.ui.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.services.ReelerMediaService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadActionsViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val media: ReelerMediaService,
) : ViewModel() {
    fun openItemOnSocialMedia(context: Context, download: DownloadEntity) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW)
            appIntent.data = Uri.parse(download.sourceUrl)
            appIntent.setPackage("com.instagram.android")
            context.startActivity(appIntent)
        } catch (e: Exception) {
            TODO("Handle case where Instagram is not installed")
            // Handle case where Instagram is not installed or deep link fails
        }
    }

    fun deleteItem(download: DownloadEntity) {
        viewModelScope.launch {
            repository.deleteDownload(download)
            val filePath = media.getContentUriFromFilePath(download.filePath!!)
            media.deleteFileFromMediaStore(filePath!!)
        }
    }

    fun shareItem(context: Context, download: DownloadEntity) {
        val filePath =
            ReelerMediaService(context).getContentUriFromFilePath(download.filePath!!)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(
                Intent.EXTRA_STREAM,
                filePath,
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share video via")
        context.startActivity(chooser)
    }
}
