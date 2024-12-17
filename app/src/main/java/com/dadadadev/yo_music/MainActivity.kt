package com.dadadadev.yo_music

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadadadev.yo_music.services.player.service.YoMusicService
import com.dadadadev.yo_music.features.audio.presentation.HomeScreen
import com.dadadadev.yo_music.features.audio.presentation.view_model.AudioEvents
import com.dadadadev.yo_music.features.audio.presentation.view_model.AudioViewModel
import com.dadadadev.yo_music.common.theme.YoMusicTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()))

        setContent {
            val permissionState = rememberMultiplePermissionsState(
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            )

            LaunchedEffect(permissionState.allPermissionsGranted) {
                if (permissionState.allPermissionsGranted) {
                    viewModel.loadAudioData()
                }
            }

            val audioState = viewModel.uiState.collectAsStateWithLifecycle().value

            YoMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (permissionState.allPermissionsGranted) {
                        HomeScreen(
                            audioState = audioState,
                            sliderProgress = audioState.sliderProgress,
                            onProgress = { viewModel.onAudioEvents(AudioEvents.SeekTo(it)) },
                            audiList = audioState.audioList,
                            onStart = {
                                viewModel.onAudioEvents(AudioEvents.PlayPause)
                                startService()
                            },
                            onItemClick = {
                                viewModel.onAudioEvents(AudioEvents.SelectedAudioChange(it))
                                startService()
                            },
                            onNext = {
                                viewModel.onAudioEvents(AudioEvents.SeekToNext)
                                startService()
                            },
                            onPrevious = {
                                viewModel.onAudioEvents(AudioEvents.SeekToPrevious)
                                startService()
                            }
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "We need permission to get music from your device",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                                Text(text = "Give permissions")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, YoMusicService::class.java)
            startForegroundService(intent)
            isServiceRunning = true
        }
    }
}