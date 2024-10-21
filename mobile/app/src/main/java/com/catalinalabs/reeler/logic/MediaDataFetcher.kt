package com.catalinalabs.reeler.logic

import android.util.Log
import com.catalinalabs.reeler.logic.commons.fetchMediaDataFromAnySource
import com.catalinalabs.reeler.logic.tiktok.fetchTiktokVideoData
import com.catalinalabs.reeler.utils.RegexExtensions.contains
import io.ktor.http.Url

class MediaDataFetcher {
    suspend fun fetchMediaData(info: MediaDownloadableExtraction): ByteArray {
        Log.d(MediaDataFetcher::class.simpleName, "Requesting video data at: ${info.url}")
        val url = Url(info.url)
        val fetch = when (url.host) {
            in Regex("(?:.*\\.)?tiktok\\.com") -> {
                ::fetchTiktokVideoData
            }

            else -> {
                ::fetchMediaDataFromAnySource
            }
        }
        val result = fetch(info)
        return result
    }
}
