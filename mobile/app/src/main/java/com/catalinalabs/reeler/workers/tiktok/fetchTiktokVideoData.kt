package com.catalinalabs.reeler.workers.tiktok

import com.catalinalabs.reeler.network.models.VideoInfoOutput
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.userAgent

suspend fun fetchTiktokVideoData(info: VideoInfoOutput): ByteArray {
    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 3600000
        }
    }
    val response = client.get(info.contentUrl) {
        headers {
            userAgent(USER_AGENT)
            accept(ContentType.parse(ACCEPT))
            append(HttpHeaders.AcceptEncoding, ACCEPT_ENCODING)
            append(HttpHeaders.AcceptLanguage, ACCEPT_LANGUAGE)
            append("Sec-Fetch-Mode", SEC_FETCH_MODE)
            append("Referer", info.sourceUrl)
            if (info.cookie != null) {
                append(HttpHeaders.Cookie, info.cookie)
            }
        }
    }
    val responseData: ByteArray = response.body()
    return responseData
}

private const val ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val ACCEPT_ENCODING = "identity"
private const val ACCEPT_LANGUAGE = "en-us,en;q=0.5"
private const val SEC_FETCH_MODE = "navigate"
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.15 Safari/537.36"
