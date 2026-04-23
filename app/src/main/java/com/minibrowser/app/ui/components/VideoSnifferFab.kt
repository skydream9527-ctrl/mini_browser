package com.minibrowser.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.ui.theme.Purple
import com.minibrowser.app.ui.theme.Red
import com.minibrowser.app.ui.theme.TextPrimary

@Composable
fun VideoSnifferFab(
    videoCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = videoCount > 0,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Red,
                    contentColor = TextPrimary
                ) {
                    Text(
                        text = if (videoCount > 99) "99+" else videoCount.toString(),
                        fontSize = 10.sp
                    )
                }
            }
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = Purple,
                contentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.VideoLibrary,
                    contentDescription = "发现视频",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
