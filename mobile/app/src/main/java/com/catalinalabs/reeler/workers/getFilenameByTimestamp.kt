package com.catalinalabs.reeler.workers

fun getTimestampString(): String {
    val timestamp = System.currentTimeMillis()
    return timestamp.toString()
}

fun getFilenameForMedia(
    caption: String?,
    sourceId: String?,
    extension: String = "mp4",
): String {
    val timestamp = getTimestampString()
    return when {
        caption != null && sourceId != null -> "$caption [${sourceId}].$extension"
        sourceId != null -> "[$sourceId].$extension"
        caption != null -> "$caption [$timestamp].$extension"
        else -> "$timestamp.$extension"
    }
}
