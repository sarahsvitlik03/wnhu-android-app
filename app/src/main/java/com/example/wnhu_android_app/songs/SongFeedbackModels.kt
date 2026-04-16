package com.example.wnhu_android_app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class SongReaction {
    LIKE,
    DISLIKE
}

@Serializable
data class SongPreferenceRequest(
    val title: String,
    val artist: String,
    val email: String
)

@Serializable
data class PullLikedSongsRequest(
    val email: String
)

@Serializable
data class PulledSongDto(
    val title: String = "",
    val artist: String = ""
)

@Serializable
data class PullLikedSongsResponse(
    val songs: List<PulledSongDto> = emptyList(),
    @SerialName("disliked_songs")
    val dislikedSongs: List<PulledSongDto> = emptyList()
)
