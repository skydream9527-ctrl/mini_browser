package com.minibrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.ui.theme.Divider
import com.minibrowser.app.ui.theme.Purple
import com.minibrowser.app.ui.theme.TextSecondary
import com.minibrowser.app.ui.theme.Toolbar

enum class NavTab(val label: String, val icon: ImageVector?) {
    HOME("首页", Icons.Default.Home),
    BOOKMARKS("书签", Icons.Default.Bookmark),
    TABS("标签页", null),
    VIDEOS("视频", Icons.Default.PlayArrow),
    MENU("菜单", Icons.Default.GridView)
}

@Composable
fun BottomNavBar(
    currentTab: NavTab,
    tabCount: Int = 1,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Toolbar)
            .drawBehind {
                drawLine(Divider, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTab.entries.forEach { tab ->
            val isActive = tab == currentTab
            val color = if (isActive) Purple else TextSecondary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .weight(1f)
            ) {
                if (tab == NavTab.TABS) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .drawBehind {
                                drawRoundRect(
                                    color = color,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabCount.toString(),
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = tab.icon!!,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = tab.label,
                    color = color,
                    fontSize = 10.sp
                )
            }
        }
    }
}
