package com.catalinalabs.reeler.ui

import kotlinx.serialization.Serializable

object Routes {
    @Serializable
    object Home
    @Serializable
    object Downloads
    @Serializable
    object Premium
    @Serializable
    data class VideoPlayer(val filePath: String)
}
