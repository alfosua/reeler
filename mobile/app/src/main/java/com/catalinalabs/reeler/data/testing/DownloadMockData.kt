package com.catalinalabs.reeler.data.testing

import com.catalinalabs.reeler.data.schema.Author
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.schema.MediaFile
import com.catalinalabs.reeler.data.schema.MediaInfo
import com.catalinalabs.reeler.data.schema.MediaLink
import com.catalinalabs.reeler.logic.MediaType
import io.realm.kotlin.ext.realmListOf

object DownloadMockData {
    val forPreview: List<DownloadLog> = listOf(
        DownloadLog().apply {
            timestamp = 1720281600000
            info = MediaInfo().apply {
                type = MediaType.Video
                caption = "POV: your golden retriever discovers the sprinkler for the first time 🐶💦"
                duration = 27.0
                source = "tiktok"
                sourceUrl = "https://www.tiktok.com/@goldenlife/video/7301234567890123456"
                author = Author().apply {
                    name = "@goldenlife"
                    username = "goldenlife"
                }
                file = MediaFile().apply {
                    filename = "POV_your_golden_retriever_discovers_the_sprinkler.mp4"
                    contentType = "video/mp4"
                    contentLength = 8_388_608
                }
            }
        },
        DownloadLog().apply {
            timestamp = 1720195200000
            info = MediaInfo().apply {
                type = MediaType.Carousel
                caption = "Golden hour in Santorini — swipe for the view from the rooftop ☀️🇬🇷"
                source = "instagram"
                sourceUrl = "https://www.instagram.com/p/C9AbCdEfGhI/"
                author = Author().apply {
                    name = "@wanderlust.diaries"
                    username = "wanderlust.diaries"
                }
                items = realmListOf(
                    MediaLink().apply {
                        child = MediaInfo().apply {
                            type = MediaType.Image
                            file = MediaFile().apply {
                                filename = "Golden_hour_in_Santorini_1.jpg"
                                contentType = "image/jpeg"
                            }
                        }
                    },
                    MediaLink().apply {
                        child = MediaInfo().apply {
                            type = MediaType.Image
                            file = MediaFile().apply {
                                filename = "Golden_hour_in_Santorini_2.jpg"
                                contentType = "image/jpeg"
                            }
                        }
                    },
                    MediaLink().apply {
                        child = MediaInfo().apply {
                            type = MediaType.Image
                            file = MediaFile().apply {
                                filename = "Golden_hour_in_Santorini_3.jpg"
                                contentType = "image/jpeg"
                            }
                        }
                    },
                )
            }
        },
        DownloadLog().apply {
            timestamp = 1720108800000
            info = MediaInfo().apply {
                type = MediaType.Image
                caption = "The launch photo everyone is talking about 🚀"
                source = "twitter"
                sourceUrl = "https://x.com/spacewatch/status/1810000000000000000"
                author = Author().apply {
                    name = "@spacewatch"
                    username = "spacewatch"
                }
                file = MediaFile().apply {
                    filename = "The_launch_photo_everyone_is_talking_about.jpg"
                    contentType = "image/jpeg"
                    contentLength = 2_097_152
                }
            }
        },
        DownloadLog().apply {
            timestamp = 1720022400000
            info = MediaInfo().apply {
                type = MediaType.Video
                caption = "I built a cabin in 30 days — full timelapse"
                duration = 754.0
                source = "youtube"
                sourceUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                author = Author().apply {
                    name = "Forest Workshop"
                    username = "forestworkshop"
                }
                file = MediaFile().apply {
                    filename = "I_built_a_cabin_in_30_days_full_timelapse.mp4"
                    contentType = "video/mp4"
                    contentLength = 157_286_400
                }
            }
        },
    )
}
