package com.catalinalabs.reeler

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.catalinalabs.reeler.services.ReelerNotificationsService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReelerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadsChannel = NotificationChannel(
                ReelerNotificationsService.DOWNLOADS_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Used to notify about download progress and completion"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(downloadsChannel)
        }
    }
}
