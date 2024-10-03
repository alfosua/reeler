package com.catalinalabs.reeler.services

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import javax.inject.Inject

class ReelerMediaService @Inject constructor(
    private val context: Context,
) {
    private val contentResolver = context.contentResolver

    fun getFileSizeFromOpenableUri(uri: Uri): Long {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (!it.isNull(sizeIndex)) {
                        return it.getLong(sizeIndex)
                    }
                }
            }
            -1 // File size not found
        } catch (e: Exception) {
            -1 // Error occurred
        }
    }

    fun saveVideo(
        data: ByteArray,
        videoInfo: VideoInfoOutput,
    ): String {
        val targetPath = Environment.DIRECTORY_DOWNLOADS + "/Reeler Videos"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, videoInfo.filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, targetPath)
        }
        val baseUri = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val uri = contentResolver.insert(baseUri, contentValues)

        Log.d(::ReelerMediaService.name, "Saving file \"${videoInfo.filename}\" to \"$targetPath\" as media ($uri)")

        contentResolver.openOutputStream(uri!!)?.use {
            it.write(data)
        }

        val projection = arrayOf(MediaStore.Downloads.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        var filePath = ""
        cursor?.use{
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }

        return filePath
    }
}
