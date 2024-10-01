package com.catalinalabs.reeler.ui.components

import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    filePath: String,
) {
    if (LocalInspectionMode.current) {
        return
    }

    Log.d("VideoPlayer", filePath)

    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val exoPlayer = ExoPlayer.Builder(context).build()
    
    LaunchedEffect(filePath) {
        val contentUri = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val selection = MediaStore.MediaColumns.DATA + " = ?"
        val selectionArgs = arrayOf(filePath)

        val cursor = contentResolver.query(
            contentUri,
            null,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val mediaStoreUri = ContentUris.withAppendedId(
                    contentUri,
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                )

                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
                val mediaItem = MediaItem.fromUri(mediaStoreUri)
                val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

                exoPlayer.apply {
                    playWhenReady = true
                    setMediaSource(mediaSource)
                    prepare()
                }
            } else {
                TODO("Handle case where file is not found in MediaStore")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
    )
}
