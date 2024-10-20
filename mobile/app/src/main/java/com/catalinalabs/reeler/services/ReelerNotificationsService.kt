package com.catalinalabs.reeler.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.catalinalabs.reeler.MainActivity
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.utils.toFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL


class ReelerNotificationsService(
    private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    suspend fun showDownloadCompletion(id: Int, download: DownloadLog) = coroutineScope {
        val largeIcon =
            async { download.info?.thumbnailUrl?.let { loadBitmapFromUrl(it) } }.await()
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_download_24)
            .setContentTitle(download.info?.caption)
            .setContentText("Download complete • ${download.info?.file?.contentLength?.toFileSize()}")
            .setContentIntent(activityPendingIntent)
            .setGroup(DOWNLOAD_TRACKER_GROUP_KEY)
            .setSilent(true)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(largeIcon)
            )
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .build()
        notificationManager.notify(DOWNLOAD_TRACKER_TAG, id, notification)
    }

    private suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            URL(url).openStream().use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: IOException) {
            Log.e(
                ::ReelerNotificationsService.name,
                "Failed to load large icon: ${e.stackTraceToString()}"
            )
            null
        }
    }

    companion object {
        private const val DOWNLOAD_TRACKER_TAG = "com.catalinalabs.reeler.download_tracker"
        private const val DOWNLOAD_TRACKER_GROUP_KEY = "com.catalinalabs.reeler.download_tracker"
        const val DOWNLOADS_CHANNEL_ID = "com.catalinalabs.reeler.downloads_channel"
    }
}