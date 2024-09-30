package com.catalinalabs.reeler.di

import android.app.Application
import android.content.Context
import com.catalinalabs.reeler.data.DownloadRepository
import com.catalinalabs.reeler.data.OfflineDownloadRepository
import com.catalinalabs.reeler.data.ReelerDatabase
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
}