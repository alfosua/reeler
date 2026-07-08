package com.catalinalabs.reeler.logic.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Single shared HTTP client for the whole app. Creating an HttpClient is
 * expensive (thread pools, connection pools), so extractors and the media
 * fetcher must reuse this one instead of building their own per request.
 */
object ReelerHttp {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(this@ReelerHttp.json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 3_600_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
        }
    }

    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
}
