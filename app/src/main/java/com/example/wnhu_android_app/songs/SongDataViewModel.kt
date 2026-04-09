package com.example.wnhu_android_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongDataViewModel : ViewModel() {

    private val _song = MutableStateFlow(
        SongModel(
            song = "Heavy",
            artist = "The Marias",
            album = "",
            genre = "",
            releaseDate = "",
            duration = 180000,
            imageURL = ""
        )
    )
    val song: StateFlow<SongModel> = _song

    fun updateFromAPI() {
        viewModelScope.launch {
            val updated = iTunesAPI.fetchSongInfo(
                title = _song.value.song,
                artist = _song.value.artist
            )
            if (updated != null) {
                _song.value = updated
            }
            println("ARTWORK: ${updated?.imageURL}")
        }
    }
}
