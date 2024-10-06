package com.catalinalabs.reeler.data

import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloadsStream(): Flow<List<DownloadEntity>>

    fun getDownloadStream(id: Int): Flow<DownloadEntity?>

    suspend fun insertDownload(item: DownloadEntity): Long

    suspend fun deleteDownload(item: DownloadEntity)

    suspend fun updateDownload(item: DownloadEntity)
}

