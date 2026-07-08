package com.catalinalabs.reeler.logic

import android.util.Log
import com.catalinalabs.reeler.logic.core.ReelerHttp
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.userAgent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import java.io.OutputStream

class MediaDataFetcher {
    /**
     * Streams the media at [info.url] into [output] so large videos never
     * have to fit in memory. Returns the number of bytes written.
     */
    suspend fun fetchMediaData(
        info: MediaDownloadableExtraction,
        output: OutputStream,
        options: MediaDataFetcherOptions? = null,
    ): Long {
        Log.d(MediaDataFetcher::class.simpleName, "Requesting media data at: ${info.url}")
        var total = 0L
        ReelerHttp.client.prepareGet(info.url) {
            headers {
                userAgent(ReelerHttp.DEFAULT_USER_AGENT)
                append(HttpHeaders.Accept, "*/*")
                append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
                info.referer?.let { append(HttpHeaders.Referrer, it) }
                info.cookie?.let { append(HttpHeaders.Cookie, it) }
            }
            options?.onProgressEmit?.let { emit ->
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength != null && contentLength > 0) {
                        emit(bytesSentTotal / contentLength.toDouble() * 100)
                    }
                }
            }
        }.execute { response ->
            val channel: ByteReadChannel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    output.write(bytes)
                    total += bytes.size
                }
            }
        }
        output.flush()
        return total
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 64 * 1024
    }
}

data class MediaDataFetcherOptions(
    val onProgressEmit: ((progress: Double) -> Unit)? = null,
)
