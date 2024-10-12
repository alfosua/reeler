package com.catalinalabs.reeler.network.models

import kotlinx.serialization.Serializable

@Serializable
data class VideoInfoOutput(
    val filename: String = "",
    val contentUrl: String = "",
    val sourceUrl: String = "",
    val source: String = "",
    val width: Int? = null,
    val height: Int? = null,
    val username: String? = null,
    val caption: String? = null,
    val duration: Double? = null,
    val userAvatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val cookie: String? = null,
)
