package com.dadadadev.yo_music.services.player.service.handler

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class YoMusicServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
) : Player.Listener {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _audioState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.Initial)
    val audioState: StateFlow<PlayerState> = _audioState.asStateFlow()

    private var progressJob: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun addMediaItemsToQueue(mediaItems: List<MediaItem>) {
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.prepare()
    }


    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    fun onPlayerEvents(
        playerEvent: PlayerEvents
    ) {
        when (playerEvent) {
            PlayerEvents.Backward -> exoPlayer.seekBack()
            PlayerEvents.Forward -> exoPlayer.seekForward()
            PlayerEvents.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvents.SeekToPrevious -> exoPlayer.seekToPrevious()
            PlayerEvents.Play -> play()
            PlayerEvents.Pause -> pause()
            PlayerEvents.Stop -> stopProgressUpdate()
            is PlayerEvents.ChangeAudio -> handleSelectedAudioChange(playerEvent.selectedAudioIndex)
            is PlayerEvents.SeekTo -> exoPlayer.seekTo(playerEvent.position)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> updateState(PlayerState.Buffering(exoPlayer.currentPosition))
            ExoPlayer.STATE_READY -> updateState(
                PlayerState.Playing(
                    isPlaying = exoPlayer.isPlaying,
                    currentIndex = exoPlayer.currentMediaItemIndex,
                    duration = exoPlayer.duration,
                    progress = exoPlayer.currentPosition
                )
            )
            else -> Unit
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateState(
            PlayerState.Playing(
                isPlaying = isPlaying,
                currentIndex = exoPlayer.currentMediaItemIndex,
                duration = exoPlayer.duration,
                progress = exoPlayer.currentPosition
            )
        )
        if (isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }

    private fun play() {
        exoPlayer.play()
        updateState(
            PlayerState.Playing(
                isPlaying = true,
                currentIndex = exoPlayer.currentMediaItemIndex,
                duration = exoPlayer.duration,
                progress = exoPlayer.currentPosition
            )
        )
        startProgressUpdate()
    }

    private fun pause() {
        exoPlayer.pause()
        stopProgressUpdate()
        updateState(
            PlayerState.Paused(
                currentIndex = exoPlayer.currentMediaItemIndex,
                progress = exoPlayer.currentPosition
            )
        )
    }

    private fun handleSelectedAudioChange(selectedAudioIndex: Int) {
        if (selectedAudioIndex != exoPlayer.currentMediaItemIndex) {
            exoPlayer.seekToDefaultPosition(selectedAudioIndex)
            updateState(
                PlayerState.Playing(
                    isPlaying = true,
                    currentIndex = selectedAudioIndex,
                    duration = exoPlayer.duration,
                    progress = exoPlayer.currentPosition
                )
            )
            exoPlayer.playWhenReady = true
            startProgressUpdate()
        } else {
            play()
        }
    }

    private fun updateState(state: PlayerState) {
        _audioState.value = state
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (exoPlayer.isPlaying) {
                delay(500)
                updateState(
                    PlayerState.Playing(
                        isPlaying = true,
                        currentIndex = exoPlayer.currentMediaItemIndex,
                        duration = exoPlayer.duration,
                        progress = exoPlayer.currentPosition
                    )
                )
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        updateState(
            PlayerState.Paused(
                currentIndex = exoPlayer.currentMediaItemIndex,
                progress = exoPlayer.currentPosition
            )
        )
    }
}
