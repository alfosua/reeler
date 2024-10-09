package com.catalinalabs.reeler.di

import android.app.Application
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.OfflineDownloadRepository
import com.catalinalabs.reeler.data.ReelerDatabase
import com.catalinalabs.reeler.network.KtorVideoDataFetcher
import com.catalinalabs.reeler.network.ProxyWorkerApiService
import com.catalinalabs.reeler.network.VideoDataFetcher
import com.catalinalabs.reeler.network.WorkerApiService
import com.catalinalabs.reeler.services.ReelerAdsService
import com.catalinalabs.reeler.services.ReelerMediaService
import com.catalinalabs.reeler.services.ReelerNotificationsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDownloadRepository(app: Application): DownloadRepository {
        return OfflineDownloadRepository(ReelerDatabase.getDatabase(app).downloadDao())
    }

    @Provides
    @Singleton
    fun provideNotificationService(app: Application): ReelerNotificationsService {
        return ReelerNotificationsService(app)
    }

    @Provides
    @Singleton
    fun provideMediaService(app: Application): ReelerMediaService {
        return ReelerMediaService(app)
    }

    @Provides
    @Singleton
    fun provideAdsService(): ReelerAdsService {
        return ReelerAdsService()
    }

    @Provides
    @Singleton
    fun provideVideoDataFetcher(): VideoDataFetcher {
        return KtorVideoDataFetcher()
    }

    @Provides
    @Singleton
    fun provideWorkerApiService(): WorkerApiService {
        return ProxyWorkerApiService()
    }
}
