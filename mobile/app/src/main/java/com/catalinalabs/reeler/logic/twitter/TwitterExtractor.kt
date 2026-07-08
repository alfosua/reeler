package com.catalinalabs.reeler.logic.twitter

import com.catalinalabs.reeler.logic.AuthorExtraction
import com.catalinalabs.reeler.logic.MediaDownloadableExtraction
import com.catalinalabs.reeler.logic.MediaInfoExtraction
import com.catalinalabs.reeler.logic.MediaType
import com.catalinalabs.reeler.logic.core.ExtractionException
import com.catalinalabs.reeler.logic.core.Extractor
import com.catalinalabs.reeler.logic.core.ReelerHttp
import com.catalinalabs.reeler.logic.getFilenameForMedia
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

object TwitterExtractor : Extractor {
    override val name = "twitter"
    override val urlPattern = Regex(
        "^(?:https?://)?(?:www\\.|m\\.|mobile\\.)?(?:twitter\\.com|x\\.com)/[^/]+/status/[0-9]+"
    )

    override suspend fun extract(url: String): MediaInfoExtraction {
        val postIds = getPostIdentityFromUrl(url)
        val rawData = fetchRawData(postIds)
        val root = rawData.data?.tweetResult?.result
        val tweet = root?.legacy
            ?: throw ExtractionException("Tweet not found; it may be private or deleted.")
        val user = root.core?.userResults?.result?.legacy
        val mediaList = tweet.extendedEntities?.media
            ?: tweet.entities?.media
            ?: emptyList()
        val caption = tweet.fullText
            ?.split('\n')?.get(0)
        val author = AuthorExtraction(
            username = user?.screenName,
            avatarUrl = user?.profileImageUrlHttps,
            profileUrl = user?.screenName?.let { "$BASE_URL/${it}/" },
            name = user?.screenName?.let { "@${it}" },
            userId = user?.screenName,
        )

        if (mediaList.isEmpty()) {
            throw ExtractionException("This tweet has no downloadable media.")
        }

        val items = mediaList.mapIndexedNotNull { index, media ->
            mediaItemOf(media, url, author, caption, "${postIds.twid}_${index + 1}")
        }
        if (items.isEmpty()) {
            throw ExtractionException("No downloadable media found in this tweet.")
        }
        if (items.size == 1) {
            return items[0].copy(
                download = items[0].download?.copy(
                    filename = when (items[0].type) {
                        MediaType.Image -> getFilenameForMedia(caption, postIds.twid, "jpg")
                        else -> getFilenameForMedia(caption, postIds.twid)
                    }
                )
            )
        }
        return MediaInfoExtraction(
            type = MediaType.Carousel,
            sourceUrl = url,
            source = name,
            caption = caption,
            thumbnailUrl = items.firstOrNull()?.thumbnailUrl,
            author = author,
            items = items,
        )
    }

    private fun mediaItemOf(
        media: Media,
        url: String,
        author: AuthorExtraction,
        caption: String?,
        sourceId: String,
    ): MediaInfoExtraction? {
        return when (media.type) {
            "video", "animated_gif" -> {
                val variant = media.videoInfo?.variants
                    ?.filter { it.contentType == "video/mp4" }
                    ?.maxByOrNull { it.bitrate ?: 0 }
                    ?: return null
                MediaInfoExtraction(
                    type = MediaType.Video,
                    sourceUrl = url,
                    source = name,
                    width = media.originalInfo?.width,
                    height = media.originalInfo?.height,
                    caption = caption,
                    duration = media.videoInfo.durationMillis?.div(1000)?.toDouble(),
                    thumbnailUrl = media.mediaUrlHttps,
                    author = author,
                    download = MediaDownloadableExtraction(
                        contentType = ContentType.Video.MP4.toString(),
                        url = variant.url ?: return null,
                        filename = getFilenameForMedia(caption, sourceId),
                    ),
                )
            }

            "photo" -> {
                val imageUrl = media.mediaUrlHttps ?: return null
                MediaInfoExtraction(
                    type = MediaType.Image,
                    sourceUrl = url,
                    source = name,
                    width = media.originalInfo?.width,
                    height = media.originalInfo?.height,
                    caption = caption,
                    thumbnailUrl = imageUrl,
                    author = author,
                    download = MediaDownloadableExtraction(
                        contentType = ContentType.Image.JPEG.toString(),
                        // name=orig gives the full-resolution original image.
                        url = "$imageUrl?name=orig",
                        filename = getFilenameForMedia(caption, sourceId, extension = "jpg"),
                    ),
                )
            }

            else -> null
        }
    }

    private data class PostIdentity(
        val twid: String,
        val url: String,
    )

    private fun getPostIdentityFromUrl(url: String): PostIdentity {
        val twid = extractTweetIdFromUrl(url)
            ?: throw IllegalArgumentException("Invalid URL")
        return PostIdentity(twid, url)
    }

    fun extractTweetIdFromUrl(url: String): String? {
        val pattern = Regex(
            pattern = "^(?:https?://)?(?:www\\.|m\\.|mobile\\.)?(?:twitter\\.com|x\\.com)/" +
                    "(?:[^/]+)/status/([0-9]+)(?:/.*)?(?:\\?.*)?\$"
        )
        val matchResult = pattern.find(url)
        return matchResult?.groupValues?.getOrNull(1)
    }

    private suspend fun fetchRawData(post: PostIdentity): RawData {
        val client = ReelerHttp.client
        val json = ReelerHttp.json
        val activateUrl = "$API_URL/guest/activate.json"
        val activateResponse = client.post(activateUrl) {
            headers {
                bearerAuth(AUTH)
            }
        }
        val activateResult: ActivateResult = activateResponse.body()
        val variables = GraphqlVariables(
            post.twid,
            withCommunity = false,
            includePromotedContent = false,
            withVoice = false,
        )
        val variablesJson = json.encodeToString(GraphqlVariables.serializer(), variables)
        val featuresJson = json.encodeToString(
            MapSerializer(String.serializer(), Boolean.serializer()),
            featureMap
        )
        val fieldTogglesJson = json.encodeToString(
            MapSerializer(String.serializer(), Boolean.serializer()),
            fieldToggles
        )
        val graphqlUrl = "$GRAPHQL_URL/$QUERY_ENDPOINT"
        val graphqlResponse = client.get(graphqlUrl) {
            url {
                parameters.append("variables", variablesJson)
                parameters.append("features", featuresJson)
                parameters.append("fieldToggles", fieldTogglesJson)
            }
            headers {
                bearerAuth(AUTH)
                append("x-guest-token", activateResult.guestToken ?: "")
            }
        }
        val resultS: String = graphqlResponse.body()
        val result: RawData = json.decodeFromString(resultS)
        return result
    }
}

@Serializable
private data class ActivateResult(
    @SerialName("guest_token")
    val guestToken: String? = null,
)

@Serializable
private data class GraphqlVariables(
    val tweetId: String,
    val withCommunity: Boolean,
    val includePromotedContent: Boolean,
    val withVoice: Boolean,
)

private const val BASE_URL = "https://x.com"
private const val GRAPHQL_URL = "https://x.com/i/api/graphql"
private const val QUERY_ENDPOINT = "2ICDjqPd81tulZcYrtpTuQ/TweetResultByRestId"
private const val API_URL = "https://api.x.com/1.1"
private const val AUTH =
    "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA"

private val featureMap = mapOf(
    "creator_subscriptions_tweet_preview_api_enabled" to true,
    "tweetypie_unmention_optimization_enabled" to true,
    "responsive_web_edit_tweet_api_enabled" to true,
    "graphql_is_translatable_rweb_tweet_is_translatable_enabled" to true,
    "view_counts_everywhere_api_enabled" to true,
    "longform_notetweets_consumption_enabled" to true,
    "responsive_web_twitter_article_tweet_consumption_enabled" to false,
    "tweet_awards_web_tipping_enabled" to false,
    "freedom_of_speech_not_reach_fetch_enabled" to true,
    "standardized_nudges_misinfo" to true,
    "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled" to true,
    "longform_notetweets_rich_text_read_enabled" to true,
    "longform_notetweets_inline_media_enabled" to true,
    "responsive_web_graphql_exclude_directive_enabled" to true,
    "verified_phone_label_enabled" to false,
    "responsive_web_media_download_video_enabled" to false,
    "responsive_web_graphql_skip_user_profile_image_extensions_enabled" to false,
    "responsive_web_graphql_timeline_navigation_enabled" to true,
    "responsive_web_enhance_cards_enabled" to false
)

private val fieldToggles = mapOf(
    "withArticleRichContentState" to false
)

@Serializable
private data class RawData(
    val data: GraphqlData? = null,
)

@Serializable
private data class GraphqlData(
    val tweetResult: TweetResult? = null,
)

@Serializable
private data class TweetResult(
    val result: Result? = null,
)

@Serializable
private data class Result(
    val core: Core? = null,
    val legacy: Legacy? = null,
)

@Serializable
private data class Legacy(
    @SerialName("full_text")
    val fullText: String? = null,
    val entities: Entities? = null,
    @SerialName("extended_entities")
    val extendedEntities: Entities? = null,
)

@Serializable
private data class Entities(
    val media: List<Media>? = null,
)

@Serializable
private data class Media(
    val url: String? = null,
    val type: String? = null,
    @SerialName("media_url_https")
    val mediaUrlHttps: String? = null,
    @SerialName("video_info")
    val videoInfo: VideoInfo? = null,
    @SerialName("original_info")
    val originalInfo: OriginalInfo? = null,
)

@Serializable
private data class OriginalInfo(
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
private data class VideoInfo(
    @SerialName("duration_millis")
    val durationMillis: Int? = null,
    val variants: List<Variant>? = null,
)

@Serializable
private data class Variant(
    val bitrate: Int? = null,
    @SerialName("content_type")
    val contentType: String? = null,
    val url: String? = null,
)

@Serializable
private data class Core(
    @SerialName("user_results")
    val userResults: UserResults? = null,
)

@Serializable
private data class UserResults(
    val result: UserResult? = null,
)

@Serializable
private data class UserResult(
    val legacy: UserLegacy? = null,
)

@Serializable
private data class UserLegacy(
    @SerialName("profile_image_url_https")
    val profileImageUrlHttps: String? = null,
    @SerialName("screen_name")
    val screenName: String? = null,
    val url: String? = null,
)
