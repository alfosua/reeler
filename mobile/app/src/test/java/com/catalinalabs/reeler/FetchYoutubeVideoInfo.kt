package com.catalinalabs.reeler

import com.catalinalabs.reeler.workers.youtube.fetchYoutubeVideoInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FetchYoutubeVideoInfo {
    @Test
    fun fetch_getVideoInfo() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val videoInfo = runBlocking {
            fetchYoutubeVideoInfo(url)
        }
        println(videoInfo)
    }
}