package com.minibrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.download.DownloadManager
import com.minibrowser.app.download.DownloadTask
import com.minibrowser.app.ui.components.DownloadProgressItem
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLibraryScreen(
    downloadManager: DownloadManager,
    onBack: () -> Unit,
    onPlayVideo: (String) -> Unit
) {
    val activeTasks by downloadManager.activeTasks.collectAsState(initial = emptyList())
    val completedTasks by downloadManager.completedTasks.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = { Text("视频", color = TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkToolbar)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkToolbar,
            contentColor = TextPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("下载中 (${activeTasks.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("已完成 (${completedTasks.size})") }
            )
        }

        when (selectedTab) {
            0 -> {
                if (activeTasks.isEmpty()) {
                    EmptyState("暂无下载任务")
                } else {
                    LazyColumn {
                        items(activeTasks, key = { it.id }) { task ->
                            DownloadProgressItem(
                                task = task,
                                onPause = { scope.launch { downloadManager.pause(task.id) } },
                                onResume = { scope.launch { downloadManager.resume(task.id) } },
                                onRetry = { scope.launch { downloadManager.retry(task.id) } },
                                onRemove = { scope.launch { downloadManager.remove(task.id) } }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (completedTasks.isEmpty()) {
                    EmptyState("暂无已完成视频")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(completedTasks, key = { it.id }) { task ->
                            CompletedVideoItem(
                                task = task,
                                onClick = {
                                    task.filePath?.let { onPlayVideo(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedVideoItem(task: DownloadTask, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(DarkToolbar),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.VideoFile,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "播放",
                tint = AccentPurple.copy(alpha = 0.8f),
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            text = task.title,
            color = TextPrimary,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = task.resolution ?: task.type.name,
            color = TextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
