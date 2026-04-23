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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.tab.TabInfo
import com.minibrowser.app.tab.TabManager
import com.minibrowser.app.ui.theme.Purple
import com.minibrowser.app.ui.theme.Red
import com.minibrowser.app.ui.theme.Black
import com.minibrowser.app.ui.theme.Surface
import com.minibrowser.app.ui.theme.Toolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun TabSwitcherScreen(
    tabManager: TabManager,
    onSelectTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val tabs by tabManager.tabs.collectAsState()
    val activeTabId by tabManager.activeTabId.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Toolbar)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tabs.size} 个标签页",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onNewTab(true) }) {
                    Icon(Icons.Default.VisibilityOff, "无痕标签", tint = TextSecondary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "关闭", tint = TextPrimary)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tabs, key = { it.id }) { tab ->
                    TabCard(
                        tab = tab,
                        isActive = tab.id == activeTabId,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { tabManager.closeTab(tab.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onNewTab(false) },
            containerColor = Purple,
            contentColor = TextPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "新建标签")
        }
    }
}

@Composable
private fun TabCard(
    tab: TabInfo,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val borderColor = when {
        tab.isIncognito -> Red
        isActive -> Purple
        else -> Toolbar
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onSelect)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(if (tab.isIncognito) Toolbar.copy(alpha = 0.7f) else Toolbar),
            contentAlignment = Alignment.Center
        ) {
            if (tab.isIncognito) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = "无痕",
                    tint = Red.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = tab.title.take(1).uppercase(),
                    color = TextSecondary,
                    fontSize = 24.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isActive) borderColor.copy(alpha = 0.15f) else Surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tab.isIncognito) {
                Icon(
                    Icons.Default.VisibilityOff,
                    null,
                    tint = Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = tab.title,
                color = TextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
