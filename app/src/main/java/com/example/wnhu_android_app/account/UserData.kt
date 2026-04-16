package com.example.wnhu_android_app

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserData : ViewModel() {
    private val repository = SongFeedbackRepository()

    var user by mutableStateOf(
        UserModel(
            firstName = "",
            lastName = "",
            email = ""
        )
    )

    var songs = mutableStateListOf<LikedSongModel>()
        private set

    var dislikedSongs = mutableStateListOf<LikedSongModel>()
        private set

    var isSyncingSongFeedback by mutableStateOf(false)
        private set

    var songFeedbackError by mutableStateOf<String?>(null)
        private set

    fun refreshSongRatings() {
        val email = ratingsEmail()

        viewModelScope.launch {
            isSyncingSongFeedback = true
            repository.loadSongRatings(email)
                .onSuccess { payload ->
                    applySongRatingsFromServer(payload)
                    songFeedbackError = null
                }
                .onFailure {
                    songFeedbackError = it.toUserMessage("Could not load song ratings.")
                }
            isSyncingSongFeedback = false
        }
    }

    private fun ratingsEmail(): String {
        val e = user.email.trim()
        return e.ifBlank { "anonymous@default.com" }
    }

    fun setSongReaction(song: SongModel, reaction: SongReaction) {
        val email = user.email.trim().ifBlank { "anonymous@default.com" }

        viewModelScope.launch {
            isSyncingSongFeedback = true
            applyOptimisticReaction(song, reaction)
            val request = when (reaction) {
                SongReaction.LIKE -> repository.likeSong(email, song)
                SongReaction.DISLIKE -> repository.dislikeSong(email, song)
            }
            request
                .onSuccess {
                    songFeedbackError = null
                }
                .onFailure {
                    songFeedbackError = it.toUserMessage("Could not save song rating.")
                }
            isSyncingSongFeedback = false
        }
    }

    fun reactionForSong(song: SongModel): SongReaction? {
        return when {
            dislikedSongs.containsSong(song.song, song.artist) -> SongReaction.DISLIKE
            songs.containsSong(song.song, song.artist) -> SongReaction.LIKE
            else -> null
        }
    }

    private fun applySongRatingsFromServer(payload: PullLikedSongsResponse) {
        songs.clear()
        dislikedSongs.clear()

        payload.dislikedSongs.forEach { rating ->
            dislikedSongs += LikedSongModel(
                songName = rating.title,
                artistName = rating.artist
            )
        }
        val dislikedKeys = dislikedSongs.map { it.songName to it.artistName }.toSet()
        payload.songs.forEach { rating ->
            if ((rating.title to rating.artist) !in dislikedKeys) {
                songs += LikedSongModel(
                    songName = rating.title,
                    artistName = rating.artist
                )
            }
        }
    }

    private fun applyOptimisticReaction(song: SongModel, reaction: SongReaction) {
        val mappedSong = LikedSongModel(
            songName = song.song,
            artistName = song.artist,
            albumName = song.album
        )

        songs.removeAll { it.songName == song.song && it.artistName == song.artist }
        dislikedSongs.removeAll { it.songName == song.song && it.artistName == song.artist }

        when (reaction) {
            SongReaction.LIKE -> songs.add(mappedSong)
            SongReaction.DISLIKE -> dislikedSongs.add(mappedSong)
        }
    }

    private fun SnapshotStateList<LikedSongModel>.containsSong(songName: String, artistName: String): Boolean {
        return any { it.songName == songName && it.artistName == artistName }
    }

    private fun Throwable.toUserMessage(fallback: String): String {
        val raw = message.orEmpty()
        return when {
            raw.contains("unexpected end of stream", ignoreCase = true) -> "The WNHU server closed the connection. Please try again."
            raw.contains("failed to connect", ignoreCase = true) -> "Could not reach the WNHU server."
            raw.contains("connection reset", ignoreCase = true) -> "The WNHU server connection was interrupted."
            raw.contains("timeout", ignoreCase = true) -> "The WNHU server took too long to respond."
            raw.isBlank() -> fallback
            else -> raw
        }
    }

    fun clearUser() {
        user = UserModel()
        songs.clear()
        dislikedSongs.clear()
        songFeedbackError = null
    }
}
