package com.catalinalabs.reeler.logic.commons

import com.catalinalabs.reeler.logic.MediaDataFetcherOptions
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

suspend fun fetchMediaDataFromAnySource(
    info: MediaDownloadableExtraction,
    options: MediaDataFetcherOptions? = null,
): ByteArray {
    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 3600000
        }
    }
    val response: HttpResponse = client.get(info.url) {
        options?.onProgressEmit?.let {
            onDownload { bytesSentTotal, contentLength ->
                val progress = bytesSentTotal / contentLength.toDouble() * 100
                it(progress)
            }
        }
    }
    val responseData: ByteArray = response.body()
    return responseData
}