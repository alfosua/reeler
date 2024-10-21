package com.catalinalabs.reeler.logic

import java.text.Normalizer

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
    val sanitized = caption?.let { sanitizeCaptionForFilename(caption) }
    return when {
        sanitized != null && sourceId != null -> "${sanitized}_[${sourceId}].$extension"
        sourceId != null -> "[$sourceId].$extension"
        sanitized != null -> "${sanitized}_[$timestamp].$extension"
        else -> "$timestamp.$extension"
    }
}

fun sanitizeCaptionForFilename(caption: String): String {
    var filename = caption.trim()
        .replace(Regex("[^A-Za-z0-9_\\-]"), "_")

    filename = Normalizer.normalize(filename, Normalizer.Form.NFD)
    filename = filename.replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")

    if (filename.length > 100) {
        filename = filename.substring(0, 100)
    }

    return filename
}
