package com.dadadadev.yo_music.services.player.service.handler

sealed class PlayerState {
    data object Initial : PlayerState()
    data class Buffering(val progress: Long) : PlayerState()

    data class Playing(
        val isPlaying: Boolean,
        val currentIndex: Int,
        val duration: Long,
        val progress: Long
    ) : PlayerState()

    data class Paused(
        val currentIndex: Int,
        val progress: Long
    ) : PlayerState()
}
