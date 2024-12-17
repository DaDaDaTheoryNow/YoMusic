package com.dadadadev.yo_music.features.audio.presentation.view_model

import com.dadadadev.yo_music.features.audio.data.local.model.Audio

data class AudioState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentIndex: Int = -1,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val sliderProgress: Float = 0f,
    val audioList: List<Audio> = listOf()
) {
    fun isPaused(): Boolean = !isPlaying && !isBuffering
}

