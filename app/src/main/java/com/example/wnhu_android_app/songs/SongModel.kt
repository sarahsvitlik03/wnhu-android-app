package com.example.wnhu_android_app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongModel(
    @SerialName("trackName") val song: String = "",
    @SerialName("artistName") val artist: String = "",
    @SerialName("collectionName") val album: String = "",
    @SerialName("primaryGenreName") val genre: String = "",
    @SerialName("releaseDate") val releaseDate: String = "",
    @SerialName("trackTimeMillis") val duration: Int = 0,
    @SerialName("artworkUrl100") val imageURL: String = ""
) {
    val highResArtwork: String
        get() = imageURL.replace("100x100", "600x600")
}

