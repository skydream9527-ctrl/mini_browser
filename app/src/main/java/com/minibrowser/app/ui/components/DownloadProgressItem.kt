package com.minibrowser.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.download.DownloadStatus
import com.minibrowser.app.download.DownloadTask
import com.minibrowser.app.ui.theme.Purple
import com.minibrowser.app.ui.theme.Red
import com.minibrowser.app.ui.theme.Toolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun DownloadProgressItem(
    task: DownloadTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Purple,
                trackColor = Toolbar
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when (task.status) {
                    DownloadStatus.DOWNLOADING -> "${(task.progress * 100).toInt()}%"
                    DownloadStatus.MERGING -> "合并中..."
                    DownloadStatus.PAUSED -> "已暂停"
                    DownloadStatus.PENDING -> "等待中"
                    DownloadStatus.FAILED -> "下载失败"
                    else -> ""
                },
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        when (task.status) {
            DownloadStatus.DOWNLOADING -> {
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, "暂停", tint = TextPrimary)
                }
            }
            DownloadStatus.PAUSED -> {
                IconButton(onClick = onResume) {
                    Icon(Icons.Default.PlayArrow, "继续", tint = TextPrimary)
                }
            }
            DownloadStatus.FAILED -> {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, "重试", tint = Red)
                }
            }
            else -> {}
        }

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, "删除", tint = TextSecondary)
        }
    }
}
