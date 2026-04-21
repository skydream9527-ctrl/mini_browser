package com.minibrowser.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.minibrowser.app.player.VideoPlayerManager

@Composable
fun VideoPlayer(
    videoUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager(context) }

    DisposableEffect(videoUrl) {
        playerManager.play(videoUrl)
        onDispose {
            playerManager.stop()
            playerManager.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = playerManager.player
                useController = true
            }
        },
        modifier = modifier
    )
}
