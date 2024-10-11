package com.catalinalabs.reeler.workers

fun getFilenameByTimestamp(extension: String = "mp4"): String {
    val timestamp = System.currentTimeMillis()
    return "video_$timestamp.$extension"
}
