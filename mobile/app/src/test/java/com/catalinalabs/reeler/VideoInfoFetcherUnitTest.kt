package com.catalinalabs.reeler

import com.catalinalabs.reeler.workers.instagram.fetchInstagramVideoInfo
import com.catalinalabs.reeler.workers.tiktok.fetchTiktokVideoInfo
import com.catalinalabs.reeler.workers.twitter.fetchTwitterVideoInfo
import com.catalinalabs.reeler.workers.youtube.fetchYoutubeVideoInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class VideoInfoFetcherUnitTest {
    @Test
    fun youtube_fetchVideoInfo() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val videoInfo = runBlocking {
            fetchYoutubeVideoInfo(url)
        }
        println(videoInfo)
    }

    @Test
    fun youtube_fetchVideoInfo_shorts() {
        val url = "https://www.youtube.com/shorts/b5dHKC8wUdc"
        val videoInfo = runBlocking {
            fetchYoutubeVideoInfo(url)
        }
        println(videoInfo)
    }

    @Test
    fun instagram_fetchVideoInfo() {
        val url = "https://www.instagram.com/reels/C_FPxueybUl/"
        val videoInfo = runBlocking {
            fetchInstagramVideoInfo(url)
        }
        println(videoInfo)
    }

    @Test
    fun tiktok_fetchVideoInfo() {
        val url = "https://www.tiktok.com/@.bestmode/video/7424109534500687136"
        val videoInfo = runBlocking {
            fetchTiktokVideoInfo(url)
        }
        println(videoInfo)
    }

    @Test
    fun twitter_fetchVideoInfo() {
        val url = "https://x.com/honordetigre/status/1845049037042413773"
        val videoInfo = runBlocking {
            fetchTwitterVideoInfo(url)
        }
        println(videoInfo)
    }
}