package com.dadadadev.yo_music.services.player.service.handler

sealed class PlayerEvents {
    data object Play : PlayerEvents()
    data object Pause : PlayerEvents()
    data class ChangeAudio(val selectedAudioIndex: Int) : PlayerEvents()
    data object Backward : PlayerEvents()
    data object SeekToNext : PlayerEvents()
    data object SeekToPrevious : PlayerEvents()
    data object Forward : PlayerEvents()
    data class SeekTo(val position: Long) : PlayerEvents()
    data object Stop : PlayerEvents()
}