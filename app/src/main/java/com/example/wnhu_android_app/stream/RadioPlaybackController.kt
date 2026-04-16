package com.example.wnhu_android_app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RadioPlaybackController {
    private val _isPrepared = MutableStateFlow(false)
    val isPrepared: StateFlow<Boolean> = _isPrepared.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    internal fun setPrepared(prepared: Boolean) {
        _isPrepared.value = prepared
    }

    internal fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}
