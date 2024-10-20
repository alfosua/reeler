package com.catalinalabs.reeler.services

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import javax.inject.Inject

class ReelerMediaService @Inject constructor(
    private val context: Context,
) {
    private val contentResolver = context.contentResolver

    fun getContentUriFromId(id: Long): Uri {
        val baseUri = getBaseContentUri()
        return ContentUris.withAppendedId(baseUri, id)
    }

    fun writeFileInMediaStore(
        data: ByteArray,
        filename: String,
        mimeType: String,
    ): WriteFileResult {
        val targetPath = Environment.DIRECTORY_DOWNLOADS + "/Reeler Videos"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, targetPath)
        }
        val baseUri = getBaseContentUri()
        val uri = contentResolver.insert(baseUri, contentValues)

        Log.d(
            ::ReelerMediaService.name,
            "Saving file \"$filename\" to \"$targetPath\" as media ($uri)"
        )

        contentResolver.openOutputStream(uri!!)?.use {
            it.write(data)
        }

        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val filePathColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val id = it.getLong(idColumn)
                val filePath = it.getString(filePathColumn)
                return WriteFileResult(id, filePath)
            }
        }

        TODO("Handle case when cursor is null or when no data is found")
    }

    data class WriteFileResult(val id: Long, val filePath: String)

    fun getContentUriFromFilePath(filePath: String): Uri? {
        val baseUri = getBaseContentUri()
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA)
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        val cursor = context.contentResolver.query(
            baseUri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val id = it.getLong(idColumn)
                return ContentUris.withAppendedId(baseUri, id)
            }
        }

        return null
    }

    fun deleteFileFromMediaStore(contentUri: Uri): Boolean {
        return try {
            val deletedRows = context.contentResolver.delete(contentUri, null, null)
            deletedRows > 0 // Return true if at least one row was deleted
        } catch (e: Exception) {
            false // Return false if an error occurred
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getBaseContentUri(): Uri {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Downloads.EXTERNAL_CONTENT_URI

            else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }
}
