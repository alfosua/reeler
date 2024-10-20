package com.catalinalabs.reeler.data

import com.catalinalabs.reeler.data.schema.DownloadLog
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.BsonObjectId

interface DownloadRepository {
    fun flowAll(): Flow<List<DownloadLog>>

    fun flowOne(id: BsonObjectId): Flow<DownloadLog?>

    suspend fun create(item: DownloadLog): DownloadLog

    suspend fun delete(item: DownloadLog)

    suspend fun update(item: DownloadLog, action: DownloadLog.() -> Unit): DownloadLog
}
