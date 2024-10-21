package com.catalinalabs.reeler.di

import android.app.Application
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.OfflineDownloadRepository
import com.catalinalabs.reeler.data.live.DownloadStatusHolder
import com.catalinalabs.reeler.data.schema.Schema
import com.catalinalabs.reeler.logic.MediaDataFetcher
import com.catalinalabs.reeler.logic.MediaInfoExtractor
import com.catalinalabs.reeler.services.ReelerAdsService
import com.catalinalabs.reeler.services.ReelerMediaService
import com.catalinalabs.reeler.services.ReelerNotificationsService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRealm(): Realm {
        val config = RealmConfiguration.create(schema = Schema)
        return Realm.open(config)
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
    fun provideVideoDataFetcher(): MediaDataFetcher {
        return MediaDataFetcher()
    }

    @Provides
    @Singleton
    fun provideWorkerApiService(): MediaInfoExtractor {
        return MediaInfoExtractor()
    }

    @Provides
    @Singleton
    fun provideDownloadStatusHolder(): DownloadStatusHolder {
        return DownloadStatusHolder()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class Repositories {
    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: OfflineDownloadRepository): DownloadRepository
}
