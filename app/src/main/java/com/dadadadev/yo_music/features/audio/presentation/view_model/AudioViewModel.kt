package com.dadadadev.yo_music.features.audio.presentation.view_model

import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.dadadadev.yo_music.features.audio.data.repository.AudioRepository
import com.dadadadev.yo_music.services.player.service.handler.PlayerEvents
import com.dadadadev.yo_music.services.player.service.handler.YoMusicServiceHandler
import com.dadadadev.yo_music.services.player.service.handler.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: YoMusicServiceHandler,
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var _uiState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState())
    val uiState: StateFlow<AudioState> = _uiState.asStateFlow()

    private var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                handlePlayerState(mediaState)
            }
        }
    }

    fun loadAudioData() {
        viewModelScope.launch {
            val audio = repository.getAudioData()
            _uiState.value = _uiState.value.copy(
                audioList = audio
            )

            setMediaItems()
        }
    }

    private fun setMediaItems() {
        _uiState.value.audioList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.displayName)
                        .build()
                )
                .build()
        }.also {
            audioServiceHandler.setMediaItemList(it)
        }
    }

    fun onAudioEvents(audioEvent: AudioEvents) = viewModelScope.launch {
        when (audioEvent) {
            is AudioEvents.PlayPause -> {
                if (_uiState.value.isPlaying) {
                    audioServiceHandler.onPlayerEvents(PlayerEvents.Pause)
                } else {
                    audioServiceHandler.onPlayerEvents(PlayerEvents.Play)
                }
            }
            AudioEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvents.SeekToNext)
            AudioEvents.SeekToPrevious -> audioServiceHandler.onPlayerEvents(PlayerEvents.SeekToPrevious)
            AudioEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvents.Backward)
            AudioEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvents.Forward)
            is AudioEvents.SelectedAudioChange -> audioServiceHandler.onPlayerEvents(
                PlayerEvents.ChangeAudio(audioEvent.index)
            )
            is AudioEvents.SeekTo -> audioServiceHandler.onPlayerEvents(
                PlayerEvents.SeekTo((duration * audioEvent.position).toLong())
            )
        }
    }

    private fun handlePlayerState(playerState: PlayerState) {
        Log.d("123", playerState.toString())
        when (playerState) {
            PlayerState.Initial -> {
                _uiState.value = AudioState()
            }
            is PlayerState.Buffering -> {
                _uiState.value = _uiState.value.copy(
                    isBuffering = true,
                    progress = playerState.progress,
                    duration = duration,
                    sliderProgress = playerState.progress.toFloat() / duration.toFloat()
                )
            }
            is PlayerState.Playing -> {
                duration = playerState.duration
                _uiState.value = _uiState.value.copy(
                    isPlaying = playerState.isPlaying,
                    isBuffering = false,
                    currentIndex = playerState.currentIndex,
                    progress = playerState.progress,
                    duration = duration,
                    sliderProgress = playerState.progress.toFloat() / duration.toFloat()
                )
            }
            is PlayerState.Paused -> {
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    currentIndex = playerState.currentIndex,
                    progress = playerState.progress,
                    duration = duration,
                    sliderProgress = playerState.progress.toFloat() / duration.toFloat()
                )
            }
        }
    }


//    @SuppressLint("DefaultLocale")
//    fun formatDuration(duration: Long): String {
//        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
//        val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes * 60
//        return String.format("%02d:%02d", minutes, seconds)
//    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvents.Stop)
        }
        super.onCleared()
    }
}
