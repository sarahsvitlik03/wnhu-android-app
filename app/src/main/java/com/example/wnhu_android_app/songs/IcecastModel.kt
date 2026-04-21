package com.example.wnhu_android_app.songs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IcecastStatus(
    val icestats: IceStats
)

@Serializable
data class IceStats(
    // Icecast often returns a list if there are multiple mount points
    // or a single object. Using a List is safer for many configurations.
    val source: List<SourceInfo>? = null,
    val host: String? = ""
)

@Serializable
data class SourceInfo(
    val currently_playing: String? = "",
    val title: String? = "",
    val genre: String? = "College Radio",
    val server_name: String? = ""
)
