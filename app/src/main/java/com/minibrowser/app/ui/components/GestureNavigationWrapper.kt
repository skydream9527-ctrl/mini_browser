package com.minibrowser.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GestureNavigationWrapper(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val threshold = with(density) { 80.dp.toPx() }
    val edgeWidth = with(density) { 24.dp.toPx() }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFromEdge by remember { mutableStateOf<Edge?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragFromEdge = when {
                            offset.x < edgeWidth -> Edge.LEFT
                            offset.x > size.width - edgeWidth -> Edge.RIGHT
                            else -> null
                        }
                        if (dragFromEdge != null) {
                            isDragging = true
                            dragOffset = 0f
                        }
                    },
                    onDragEnd = {
                        if (isDragging && dragFromEdge != null) {
                            when (dragFromEdge) {
                                Edge.LEFT -> if (dragOffset > threshold) onSwipeRight()
                                Edge.RIGHT -> if (abs(dragOffset) > threshold) onSwipeLeft()
                                null -> {}
                            }
                        }
                        isDragging = false
                        dragOffset = 0f
                        dragFromEdge = null
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = 0f
                        dragFromEdge = null
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (dragFromEdge != null) {
                            dragOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        content()

        AnimatedVisibility(
            visible = isDragging && dragFromEdge == Edge.LEFT && dragOffset > 20f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.coerceIn(0f, threshold * 1.2f).roundToInt() - 40, 0) }
                    .size(40.dp)
                    .background(
                        AccentPurple.copy(alpha = (dragOffset / threshold).coerceIn(0.3f, 0.9f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isDragging && dragFromEdge == Edge.RIGHT && dragOffset < -20f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((dragOffset.coerceIn(-threshold * 1.2f, 0f) + 40).roundToInt(), 0) }
                    .size(40.dp)
                    .background(
                        AccentPurple.copy(alpha = (abs(dragOffset) / threshold).coerceIn(0.3f, 0.9f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "前进",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private enum class Edge { LEFT, RIGHT }
