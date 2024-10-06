package com.catalinalabs.reeler.data

import kotlinx.coroutines.flow.Flow

class OfflineDownloadRepository(
    private val downloadDao: DownloadDao,
) : DownloadRepository {
    override fun getAllDownloadsStream(): Flow<List<DownloadEntity>> = downloadDao.getAll()

    override fun getDownloadStream(id: Int): Flow<DownloadEntity> = downloadDao.getOne(id)

    override suspend fun insertDownload(item: DownloadEntity): Long = downloadDao.insert(item)

    override suspend fun deleteDownload(item: DownloadEntity) = downloadDao.delete(item)

    override suspend fun updateDownload(item: DownloadEntity) = downloadDao.update(item)
}
