package com.catalinalabs.reeler.workers

import com.catalinalabs.reeler.data.schema.Author
import com.catalinalabs.reeler.data.schema.MediaFile
import com.catalinalabs.reeler.data.schema.MediaInfo
import com.catalinalabs.reeler.data.schema.MediaLink
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable

@Serializable
data class MediaInfoExtraction(
    val type: String = MediaType.Video,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Double? = null,
    val caption: String? = null,
    val author: AuthorExtraction? = null,
    val sourceUrl: String? = null,
    val source: String? = null,
    val thumbnailUrl: String? = null,
    val download: MediaDownloadableExtraction? = null,
    val items: List<MediaInfoExtraction>? = null,
)

@Serializable
data class AuthorExtraction(
    val name: String? = null,
    val username: String? = null,
    val userId: String? = null,
    val avatarUrl: String? = null,
    val profileUrl: String? = null,
)

@Serializable
data class MediaDownloadableExtraction(
    val filename: String = "",
    val contentType: String = ContentType.Video.MP4.toString(),
    val url: String = "",
    val referer: String? = null,
    val cookie: String? = null,
)

val MediaInfoExtraction.downloadables: List<MediaDownloadableExtraction>
    get() {
        val targets = this.items?.let {
            listOf(this.download) + it.map { item -> item.download }
        } ?: listOf(this.download)
        val filtered = targets.filterNotNull()
        return filtered
    }

fun MediaInfoExtraction.asMediaInfo(): MediaInfo {
    val it = this
    return MediaInfo().apply {
        source = it.source
        sourceUrl = it.sourceUrl
        type = it.type
        width = it.width
        height = it.height
        caption = it.caption
        duration = it.duration
        thumbnailUrl = it.thumbnailUrl
        file = it.download?.asMediaDownload()
        author = it.author?.asAuthor()
        items.addAll(it.items?.map {
            MediaLink().apply { child = it.asMediaInfo() }
        } ?: emptyList())
    }
}

fun MediaDownloadableExtraction.asMediaDownload(): MediaFile {
    val it = this
    return MediaFile().apply {
        filename = it.filename
        contentType = it.contentType
        url = it.url
    }
}

fun AuthorExtraction.asAuthor(): Author {
    val it = this
    return Author().apply {
        name = it.name
        username = it.username
        avatarUrl = it.avatarUrl
        profileUrl = it.profileUrl
        userId = it.userId
    }
}
