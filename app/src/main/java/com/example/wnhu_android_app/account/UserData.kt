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
        val email = user.email.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            isSyncingSongFeedback = true
            repository.loadLikedSongs(email)
                .onSuccess { likedSongs ->
                    applyLikedSongs(likedSongs)
                    songFeedbackError = null
                }
                .onFailure {
                    songFeedbackError = it.message ?: "Could not load song ratings."
                }
            isSyncingSongFeedback = false
        }
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
                    if (reaction == SongReaction.LIKE) {
                        refreshSongRatings()
                    }
                }
                .onFailure {
                    songFeedbackError = it.message ?: "Could not save song rating."
                    refreshSongRatings()
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

    private fun applyLikedSongs(ratings: List<PulledSongDto>) {
        songs.clear()

        ratings.forEach { rating ->
            songs += LikedSongModel(
                songName = rating.title,
                artistName = rating.artist
            )
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

    fun clearUser() {
        user = UserModel()
        songs.clear()
        dislikedSongs.clear()
        songFeedbackError = null
    }
}
