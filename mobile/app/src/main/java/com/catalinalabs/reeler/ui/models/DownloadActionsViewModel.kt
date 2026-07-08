package com.catalinalabs.reeler.ui.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.schema.MediaFile
import com.catalinalabs.reeler.data.schema.files
import com.catalinalabs.reeler.services.ReelerMediaService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadActionsViewModel @Inject constructor(
    private val repository: DownloadRepository,
    private val media: ReelerMediaService,
) : ViewModel() {
    fun openItemOnSocialMedia(context: Context, download: DownloadLog) {
        val sourceUrl = download.info?.sourceUrl ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.no_app_found_to_open_this_link),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun deleteItem(log: DownloadLog) {
        viewModelScope.launch {
            for (target in log.files) {
                val uri = getUriFromTarget(target)
                if (uri != null) {
                    media.deleteFileFromMediaStore(uri)
                }
            }
            repository.delete(log)
        }
    }

    fun shareItem(context: Context, download: DownloadLog) {
        val uri = getUriFromTarget(download.info?.file)
        if (uri == null) {
            Toast.makeText(
                context,
                context.getString(R.string.the_file_could_not_be_shared),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = download.info?.file?.contentType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share video via")
        context.startActivity(chooser)
    }

    private fun getUriFromTarget(target: MediaFile?): Uri? {
        val mediaStoreId = target?.mediaStoreId
        val filePath = target?.filePath
        val uri = if (mediaStoreId != null) {
            media.getContentUriFromId(mediaStoreId, target.contentType)
        } else if (filePath != null) {
            media.getContentUriFromFilePath(filePath, target.contentType)
        } else {
            null
        }
        return uri
    }
}
