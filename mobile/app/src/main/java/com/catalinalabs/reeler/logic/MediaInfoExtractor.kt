package com.catalinalabs.reeler.logic

import com.catalinalabs.reeler.logic.core.Extractor
import com.catalinalabs.reeler.logic.core.UnsupportedSourceException
import com.catalinalabs.reeler.logic.facebook.FacebookExtractor
import com.catalinalabs.reeler.logic.instagram.InstagramExtractor
import com.catalinalabs.reeler.logic.pinterest.PinterestExtractor
import com.catalinalabs.reeler.logic.reddit.RedditExtractor
import com.catalinalabs.reeler.logic.tiktok.TiktokExtractor
import com.catalinalabs.reeler.logic.twitter.TwitterExtractor
import com.catalinalabs.reeler.logic.youtube.YoutubeExtractor

class MediaInfoExtractor {
    private val extractors: List<Extractor> = listOf(
        YoutubeExtractor,
        TiktokExtractor,
        InstagramExtractor,
        TwitterExtractor,
        FacebookExtractor,
        RedditExtractor,
        PinterestExtractor,
    )

    val supportedPlatforms: List<String>
        get() = extractors.map { it.name }

    fun isSupported(sourceUrl: String): Boolean =
        extractors.any { it.matches(sourceUrl) }

    suspend fun extractMediaInfo(sourceUrl: String): MediaInfoExtraction {
        val extractor = extractors.firstOrNull { it.matches(sourceUrl) }
            ?: throw UnsupportedSourceException(sourceUrl, supportedPlatforms)
        return extractor.extract(sourceUrl)
    }
}
