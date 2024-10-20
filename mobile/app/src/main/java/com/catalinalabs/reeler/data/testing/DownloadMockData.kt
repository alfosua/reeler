package com.catalinalabs.reeler.data.testing

import com.catalinalabs.reeler.data.schema.DownloadLog

object DownloadMockData {
    val forPreview: List<DownloadLog> = listOf(
        DownloadLog().apply {
            timestamp = 0L
        },
    )
}
