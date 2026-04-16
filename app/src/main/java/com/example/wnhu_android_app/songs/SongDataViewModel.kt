package com.example.wnhu_android_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SongDataViewModel : ViewModel() {

    private val _song = MutableStateFlow(SongModel())
    val song: StateFlow<SongModel> = _song

    fun updateFromAPI() {
        viewModelScope.launch {
            val liveData = fetchLiveMetadata()
            if (liveData != null) {
                val artist = liveData.artist ?: "Unknown"
                val title = liveData.title ?: "Unknown"

                // Now search iTunes using the live info
                val itunesSong = iTunesAPI.fetchSongInfo(title, artist)

                if (itunesSong != null) {
                    _song.value = itunesSong
                } else {
                    // Fallback: update UI with Icecast info even if iTunes fails
                    _song.value = SongModel(song = title, artist = artist)
                }
            } else {
                _song.value = SongModel(song = "Unknown", artist = "Unknown")
            }
        }
    }
}
