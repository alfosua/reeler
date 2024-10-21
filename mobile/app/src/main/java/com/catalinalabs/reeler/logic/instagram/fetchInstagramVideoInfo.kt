package com.catalinalabs.reeler.logic.instagram

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.getFilenameForMedia
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.get
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.StringValues
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun fetchInstagramVideoInfo(url: String): MediaInfoExtraction {
    val postIds = getPostIdentityFromUrl(url)
    val rawData = fetchRawData(postIds)
    val media = rawData.data?.xdtShortcodeMedia
    val owner = media?.owner
    val caption = media?.edgeMediaToCaption?.edges
        ?.getOrNull(0)?.node?.text
        ?.split('\n')?.get(0)

    val result = MediaInfoExtraction(
        type = MediaType.Video,
        sourceUrl = url,
        source = "instagram",
        width = media?.dimensions?.width,
        height = media?.dimensions?.height,
        caption = caption,
        duration = media?.videoDuration,
        thumbnailUrl = media?.thumbnailSrc,
        author = AuthorExtraction(
            username = owner?.username,
            avatarUrl = owner?.profilePicUrl,
            profileUrl = owner?.let { "$BASE_URL/${it.username}/" },
            name = owner?.let { "@${it.username}" },
            userId = owner?.username,
        ),
        download = MediaDownloadableExtraction(
            contentType = ContentType.Video.MP4.toString(),
            url = media?.videoUrl ?: "",
            filename = getFilenameForMedia(caption, postIds.pk.toString()),
        ),
    )

    return result
}

private data class PostIdentity(
    val shortCode: String,
    val pk: Long,
    val url: String,
)

private fun getPostIdentityFromUrl(url: String): PostIdentity {
    val shortCode = extractShortCodeFromUrl(url)
        ?: throw IllegalArgumentException("Invalid URL")
    val pk = getPkFromShortCode(shortCode)
    return PostIdentity(shortCode, pk, url)
}

private fun extractShortCodeFromUrl(url: String): String? {
    val pattern = Regex(
        pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:instagram\\.com|instagr\\.am)/" +
                "(?:p|reels|reel|tv)/([a-zA-Z0-9_-]+)(?:/.*)?(?:\\?.*)?\$"
    )
    val matchResult = pattern.find(url)
    return matchResult?.groupValues?.getOrNull(1)
}

private fun getPkFromShortCode(shortCode: String): Long {
    return decodeBaseN(shortCode.take(11), table = SHORTCODE_CHARSET)
}

fun decodeBaseN(string: String, n: Int? = null, table: String? = null): Long {
    val charset = baseNTable(n, table)
    val lookupTable = charset.withIndex().associate { it.value to it.index }
    var result = 0L
    val base = charset.length
    for (char in string) {
        result = result * base + (lookupTable[char] ?: error("Invalid character: $char"))
    }
    return result
}

fun baseNTable(n: Int?, table: String?): String {
    if (table == null && n == null) {
        throw IllegalArgumentException("Either table or n must be specified")
    }
    val baseTable =
        (table ?: "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").take(
            n ?: table!!.length
        )

    if (n != null && n != baseTable.length) {
        throw IllegalArgumentException("base $n exceeds table length ${baseTable.length}")
    }
    return baseTable
}

private const val SHORTCODE_CHARSET =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

val json = Json {
    ignoreUnknownKeys = true
}

private suspend fun fetchRawData(post: PostIdentity): RawData {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }
    val checkUrl = "$API_URL/web/get_ruling_for_content/?content_type=MEDIA&target_id=${post.pk}"
    val checkResponse = client.get(checkUrl) {
        headers {
            appendAll(apiBaseHeaders)
        }
    }
    val csrfToken = checkResponse.setCookie()["csrftoken"]?.value
    val variables = GraphqlVariables(post.shortCode, 3, 40, 24, true)
    val variablesJson = json.encodeToString(GraphqlVariables.serializer(), variables)
    val graphqlResponse = client.get(GRAPHQL_QUERY_URL) {
        url {
            parameters.append("doc_id", "8845758582119845")
            parameters.append("variables", variablesJson)
        }
        headers {
            appendAll(apiBaseHeaders)
            append("X-CSRFToken", csrfToken ?: "")
            append("X-Requested-With", "XMLHttpRequest")
            append("Referer", post.url)
        }
    }
    val result: RawData = graphqlResponse.body()
    return result
}

private const val BASE_URL = "https://www.instagram.com"
private const val GRAPHQL_QUERY_URL = "https://www.instagram.com/graphql/query"
private const val API_URL = "https://i.instagram.com/api/v1"

private val apiBaseHeaders = StringValues.build {
    append(HttpHeaders.Origin, "https://www.instagram.com")
    append(HttpHeaders.Accept, "*/*")
    append("X-IG-WWW-Claim", "0")
    append("X-IG-App-ID", "936619743392459")
    append("X-ASBD-ID", "198387")
}

@Serializable
private data class GraphqlVariables(
    val shortcode: String,
    @SerialName("child_comment_count")
    val childCommentCount: Int,
    @SerialName("fetch_comment_count")
    val fetchCommentCount: Int,
    @SerialName("parent_comment_count")
    val parentCommentCount: Int,
    @SerialName("has_threaded_comments")
    val hasThreadedComments: Boolean,
)

private const val USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"

@Serializable
private data class RawData(
    val data: GraphqlData? = null,
    val status: String? = null,
)

@Serializable
private data class GraphqlData(
    @SerialName("xdt_shortcode_media")
    val xdtShortcodeMedia: XdtShortcodeMedia? = null,
)

@Serializable
private data class XdtShortcodeMedia(
    @SerialName("video_url")
    val videoUrl: String? = null,
    @SerialName("is_video")
    val isVideo: Boolean? = null,
    @SerialName("display_url")
    val displayUrl: String? = null,
    val owner: Owner? = null,
    val dimensions: Dimensions? = null,
    @SerialName("video_duration")
    val videoDuration: Double? = null,
    @SerialName("edge_media_to_caption")
    val edgeMediaToCaption: Caption? = null,
    @SerialName("thumbnail_src")
    val thumbnailSrc: String? = null,
)

@Serializable
private data class Dimensions(
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
private data class Owner(
    val username: String? = null,
    @SerialName("profile_pic_url")
    val profilePicUrl: String? = null,
)

@Serializable
private data class Caption(
    val edges: List<Edge>? = null,
)

@Serializable
private data class Edge(
    val node: Node? = null,
)

@Serializable
private data class Node(
    val text: String? = null,
)
