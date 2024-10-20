package com.catalinalabs.reeler.data

import com.catalinalabs.reeler.data.schema.DownloadLog
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.BsonObjectId
import javax.inject.Inject

class OfflineDownloadRepository @Inject constructor(
    private val realm: Realm,
) : DownloadRepository {
    private fun query(): RealmQuery<DownloadLog> = realm.query()

    override fun flowAll(): Flow<List<DownloadLog>> {
        return query().sort("timestamp", Sort.DESCENDING).asFlow()
            .map { change -> change.list.map { it } }
    }

    override fun flowOne(id: BsonObjectId): Flow<DownloadLog?> {
        return query().asFlow()
            .map { change -> change.list.firstOrNull { it.id == id } }
    }

    override suspend fun create(item: DownloadLog): DownloadLog {
        return realm.write {
            copyToRealm(item)
        }
    }

    override suspend fun delete(item: DownloadLog) {
        realm.write {
            findLatest(item)?.let { delete(it) }
        }
    }

    override suspend fun update(item: DownloadLog, action: DownloadLog.() -> Unit): DownloadLog {
        return realm.write {
            findLatest(item)?.also { live ->
                action(live)
            } ?: throw Exception("Item not found")
        }
    }
}
