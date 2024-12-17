package com.dadadadev.yo_music.features.audio.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dadadadev.yo_music.common.theme.Dimensions
import com.dadadadev.yo_music.features.audio.data.local.model.Audio
import com.dadadadev.yo_music.features.audio.presentation.components.DraggableBottomSheet
import com.dadadadev.yo_music.features.audio.presentation.view_model.AudioState
import com.dadadadev.yo_music.features.audio.presentation.components.dismissedAnchor
import com.dadadadev.yo_music.features.audio.presentation.components.rememberBottomSheetState
import kotlin.math.floor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    audioState: AudioState,
    sliderProgress: Float,
    onProgress: (Float) -> Unit,
    audiList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val windowsInsets = WindowInsets.systemBars
        val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

        val playerBottomSheetState = rememberBottomSheetState(
            dismissedBound = 0.dp,
            collapsedBound = Dimensions.collapsedPlayer + bottomDp,
            initialAnchor = dismissedAnchor,
            expandedBound = maxHeight
        )

        LaunchedEffect(audioState) {
            if ((audioState.progress != 0L || audioState.isPlaying) && playerBottomSheetState.isDismissed) {
                playerBottomSheetState.collapseSoft()
            }
        }

//        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//
//            topBar = {
//                TopAppBar(
//                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
//                    ),
//                    title = {},
//                    actions = {
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                imageVector = Icons.Filled.Add,
//                                contentDescription = "Import Song"
//                            )
//                        }
//                    },
//                    scrollBehavior = scrollBehavior,
//                )
//            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = it
            ) {
                itemsIndexed(audiList) { index, audio ->
                    AudioItem(
                        audio = audio,
                        onItemClick = { onItemClick(index) }
                    )
                }
            }
        }

        val animatedProgress by animateFloatAsState(
            targetValue = sliderProgress,
            label = "audio progress"
        )

        val progressLineColor = MaterialTheme.colorScheme.primary
        DraggableBottomSheet(
            state = playerBottomSheetState,
            modifier = Modifier.align(Alignment.BottomCenter),
            collapsedContent = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .fillMaxSize()
                        .padding(bottom = (bottomDp.value / 1.05).dp)
                        .drawBehind {
                            drawLine(
                                color = progressLineColor,
                                start = Offset(x = 0f, y = 1.dp.toPx()),
                                end = Offset(x = size.width * animatedProgress, y = 1.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                ) {
                    MediaPlayerController(
                        isAudioPlaying = audioState.isPlaying,
                        onStart = onStart,
                        onNext = onNext,
                        onPrevious = onPrevious
                    )
                }
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                ExpandedBottomBarPlayer(
                    sliderProgress = sliderProgress,
                    onProgress = onProgress,
                    audioState = audioState,
                    onStart = onStart,
                    onNext = onNext,
                    onPrevious = onPrevious,
                )
            }
        }
    }
}

@Composable
fun ExpandedBottomBarPlayer(
    sliderProgress: Float,
    onProgress: (Float) -> Unit,
    audioState: AudioState,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = sliderProgress,
        label = "audio progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier
                .size(200.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {}

        Spacer(Modifier.height(20.dp))

        Slider(
            value = if (animatedProgress.isNaN()) 0f else animatedProgress,
            onValueChange = { value -> onProgress(value) },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
        )

        MediaPlayerController(
            isAudioPlaying = audioState.isPlaying,
            onStart = onStart,
            onNext = onNext,
            onPrevious = onPrevious
        )
    }
}

@Composable
fun AudioItem(
    audio: Audio,
    onItemClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onItemClick()
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            Text(
                text = timeStampToDuration(audio.duration)
            )
        }
    }
}

private fun timeStampToDuration(position: Long): String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        IconButton(onClick = onPrevious) {
            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
        }
        IconButton(onClick = onStart) {
            Icon(
                imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
        IconButton(onClick = onNext) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
        }
    }
}
