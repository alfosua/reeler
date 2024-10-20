package com.catalinalabs.reeler.data.schema

import com.catalinalabs.reeler.workers.MediaType
import io.ktor.http.ContentType
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId

class DownloadLog : RealmObject {
    @PrimaryKey
    @PersistedName("_id")
    var id: BsonObjectId = BsonObjectId()
    var timestamp: Long = 0
    var info: MediaInfo? = null
}

class MediaInfo : EmbeddedRealmObject {
    var type: String = MediaType.Video
    var width: Int? = null
    var height: Int? = null
    var duration: Double? = null
    var caption: String? = null
    var author: Author? = null
    var source: String? = null
    var sourceUrl: String? = null
    var thumbnailUrl: String? = null
    var file: MediaFile? = null
    var items: RealmList<MediaLink> = realmListOf()
}

class MediaLink : RealmObject {
    var parent: MediaInfo? = null
    var child: MediaInfo? = null
}

class Author : EmbeddedRealmObject {
    var name: String? = null
    var username: String? = null
    var userId: String? = null
    var avatarUrl: String? = null
    var profileUrl: String? = null
}

class MediaFile : EmbeddedRealmObject {
    var filename: String = ""
    var contentType: String = ContentType.Video.MP4.toString()
    var url: String = ""
    var contentLength: Long? = null
    var filePath: String? = null
    var mediaStoreId: Long? = null
}

val DownloadLog.files: List<MediaFile>
    inline get() {
        val download = this.info?.file
        val targets = this.info?.items?.let {
            listOf(download) + it.map { item -> item.child?.file }
        } ?: listOf(download)
        val filtered = targets.filterNotNull()
        return filtered
    }
