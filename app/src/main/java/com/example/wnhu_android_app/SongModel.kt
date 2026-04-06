package com.example.wnhu_android_app

data class SongModel(
    val song: String,
    val artist: String,
    val album: String,
    val genre: String,
    val releaseDate: String,
    val duration: Int,
    val imageURL: String
) {
    val highResArtwork: String
        get() = imageURL.replace("100x100", "600x600")

    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
