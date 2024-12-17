package com.dadadadev.yo_music.services.player.service

import android.app.Service
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dadadadev.yo_music.services.player.notification.YoMusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class YoMusicService : MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: YoMusicNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession.player.repeatMode = Player.REPEAT_MODE_ALL
        notificationManager.startNotificationService(this, mediaSession)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        super.onDestroy()


        mediaSession.apply {
            if (mediaSession.player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.pause()
                player.stop()
            }
        }

        // delete notification
        notificationManager.stopNotificationService()

        // stopping service
        stopForeground(Service.STOP_FOREGROUND_DETACH)
    }
}