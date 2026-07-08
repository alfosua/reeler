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

    fun getContentUriFromId(id: Long, mimeType: String? = null): Uri {
        val baseUri = getBaseContentUri(mimeType)
        return ContentUris.withAppendedId(baseUri, id)
    }

    /**
     * Creates the media entry and hands the caller an OutputStream to write
     * into, so downloads can be streamed to disk instead of buffered in
     * memory. Images land in Pictures/Reeler, everything else in
     * Downloads/Reeler Videos.
     */
    suspend fun writeFileInMediaStore(
        filename: String,
        mimeType: String,
        write: suspend (java.io.OutputStream) -> Unit,
    ): WriteFileResult {
        val isImage = mimeType.startsWith("image/")
        val targetPath = if (isImage) {
            Environment.DIRECTORY_PICTURES + "/Reeler"
        } else {
            Environment.DIRECTORY_DOWNLOADS + "/Reeler Videos"
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, targetPath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val baseUri = getBaseContentUri(mimeType)
        val uri = contentResolver.insert(baseUri, contentValues)
            ?: throw IllegalStateException("Could not create media entry for \"$filename\"")

        Log.d(
            ::ReelerMediaService.name,
            "Saving file \"$filename\" to \"$targetPath\" as media ($uri)"
        )

        try {
            contentResolver.openOutputStream(uri)?.use {
                write(it)
            } ?: throw IllegalStateException("Could not open output stream for $uri")
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            throw e
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val publish = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            contentResolver.update(uri, publish, null, null)
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

        throw IllegalStateException("Could not resolve saved media entry for \"$filename\"")
    }

    data class WriteFileResult(val id: Long, val filePath: String)

    fun getContentUriFromFilePath(filePath: String, mimeType: String? = null): Uri? {
        val baseUri = getBaseContentUri(mimeType)
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
    fun getBaseContentUri(mimeType: String? = null): Uri {
        return when {
            mimeType?.startsWith("image/") == true ->
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Downloads.EXTERNAL_CONTENT_URI

            else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }
}
