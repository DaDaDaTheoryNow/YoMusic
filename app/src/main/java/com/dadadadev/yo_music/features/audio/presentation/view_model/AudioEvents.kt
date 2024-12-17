package com.dadadadev.yo_music.features.audio.presentation.view_model

sealed class AudioEvents {
    data object PlayPause : AudioEvents()
    data class SelectedAudioChange(val index: Int) : AudioEvents()
    data class SeekTo(val position: Float) : AudioEvents()
    data object SeekToNext : AudioEvents()
    data object SeekToPrevious : AudioEvents()
    data object Backward : AudioEvents()
    data object Forward : AudioEvents()
}