package com.dadadadev.yo_music.features.audio.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.IntOffset

@Composable
fun DraggableBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .offset {
                val y = (state.expandedBound - state.value)
                    .roundToPx()
                    .coerceAtLeast(0)
                IntOffset(x = 0, y = y)
            }
            .pointerInput(state) {
                val velocityTracker = VelocityTracker()

                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        velocityTracker.addPointerInputChange(change)
                        state.dispatchRawDelta(dragAmount)
                    },
                    onDragCancel = {
                        velocityTracker.resetTracking()
                    },
                    onDragEnd = {
                        val velocity = -velocityTracker.calculateVelocity().y
                        velocityTracker.resetTracking()
                        state.performFling(velocity, onDismiss)
                    }
                )
            }
            .fillMaxSize()
    ) {
        if (!state.isCollapsed) {
            BackHandler(onBack = state::collapseSoft)
            content()
        }

        if (!state.isExpanded && (onDismiss == null || !state.isDismissed)) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - (state.progress * 2).coerceAtMost(1f)
                    }
                    .clickable(onClick = state::expandSoft)
                    .fillMaxWidth()
                    .height(state.collapsedBound),
                content = collapsedContent
            )
        }
    }
}
