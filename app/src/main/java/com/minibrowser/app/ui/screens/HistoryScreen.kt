package com.minibrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.data.HistoryEntity
import com.minibrowser.app.data.HistoryRepository
import com.minibrowser.app.ui.theme.AccentRed
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyRepository: HistoryRepository,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val historyItems by (if (searchQuery.isBlank()) {
        historyRepository.allHistory
    } else {
        historyRepository.search(searchQuery)
    }).collectAsState(initial = emptyList())

    val grouped = remember(historyItems) { groupByDate(historyItems) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = { Text("历史记录", color = TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(onClick = {
                    scope.launch { historyRepository.clearAll() }
                }) {
                    Icon(Icons.Default.DeleteSweep, "清除全部", tint = AccentRed)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkToolbar)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(DarkToolbar, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(TextPrimary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text("搜索历史", color = TextSecondary, fontSize = 14.sp)
                    }
                    inner()
                }
            )
        }

        if (historyItems.isEmpty()) {
            Spacer(Modifier.height(100.dp))
            Text(
                text = if (searchQuery.isBlank()) "暂无历史记录" else "无搜索结果",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                grouped.forEach { (dateLabel, items) ->
                    item {
                        Text(
                            text = dateLabel,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(items, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(entry.url) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row {
                                    Text(
                                        text = entry.url,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (entry.visitCount > 1) {
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${entry.visitCount}次",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun groupByDate(items: List<HistoryEntity>): List<Pair<String, List<HistoryEntity>>> {
    val calendar = Calendar.getInstance()
    val todayStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 86400000L

    val today = mutableListOf<HistoryEntity>()
    val yesterday = mutableListOf<HistoryEntity>()
    val earlier = mutableListOf<HistoryEntity>()

    for (item in items) {
        when {
            item.lastVisitAt >= todayStart -> today.add(item)
            item.lastVisitAt >= yesterdayStart -> yesterday.add(item)
            else -> earlier.add(item)
        }
    }

    return buildList {
        if (today.isNotEmpty()) add("今天" to today)
        if (yesterday.isNotEmpty()) add("昨天" to yesterday)
        if (earlier.isNotEmpty()) add("更早" to earlier)
    }
}
