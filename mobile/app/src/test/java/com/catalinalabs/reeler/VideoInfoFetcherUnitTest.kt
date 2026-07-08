package com.catalinalabs.reeler

import com.catalinalabs.reeler.logic.instagram.InstagramExtractor
import com.catalinalabs.reeler.logic.tiktok.TiktokExtractor
import com.catalinalabs.reeler.logic.twitter.TwitterExtractor
import com.catalinalabs.reeler.logic.youtube.YoutubeExtractor
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Live network tests against real posts; they hit each platform's API, so
 * they can fail when a post is removed or the platform blocks the runner.
 */
class VideoInfoFetcherUnitTest {
    @Test
    fun youtube_fetchVideoInfo() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val videoInfo = runBlocking {
            YoutubeExtractor.extract(url)
        }
        println(videoInfo)
    }

    @Test
    fun youtube_fetchVideoInfo_shorts() {
        val url = "https://www.youtube.com/shorts/b5dHKC8wUdc"
        val videoInfo = runBlocking {
            YoutubeExtractor.extract(url)
        }
        println(videoInfo)
    }

    @Test
    fun instagram_fetchVideoInfo() {
        val url = "https://www.instagram.com/reels/C_FPxueybUl/"
        val videoInfo = runBlocking {
            InstagramExtractor.extract(url)
        }
        println(videoInfo)
    }

    @Test
    fun tiktok_fetchVideoInfo() {
        val url = "https://www.tiktok.com/@.bestmode/video/7424109534500687136"
        val videoInfo = runBlocking {
            TiktokExtractor.extract(url)
        }
        println(videoInfo)
    }

    @Test
    fun twitter_fetchVideoInfo() {
        val url = "https://x.com/honordetigre/status/1845049037042413773"
        val videoInfo = runBlocking {
            TwitterExtractor.extract(url)
        }
        println(videoInfo)
    }
}
