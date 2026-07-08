package com.catalinalabs.reeler.logic.core

import com.catalinalabs.reeler.logic.MediaInfoExtraction

/**
 * A media info extractor for a single platform, in the spirit of yt-dlp's
 * InfoExtractor: it declares which URLs it can handle and knows how to turn
 * a page URL into downloadable media info.
 */
interface Extractor {
    /** Short platform identifier, e.g. "instagram". */
    val name: String

    /** Pattern matched against the full source URL to decide support. */
    val urlPattern: Regex

    fun matches(url: String): Boolean = urlPattern.containsMatchIn(url)

    suspend fun extract(url: String): MediaInfoExtraction
}

class UnsupportedSourceException(
    val url: String,
    supported: Collection<String>,
) : Exception(
    "This link isn't supported yet. Supported platforms: ${supported.joinToString(", ")}."
)

class ExtractionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
