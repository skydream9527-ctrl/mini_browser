package com.minibrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.ui.components.SearchBar
import com.minibrowser.app.ui.components.SearchEngineSelector
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    selectedEngineId: String,
    onNavigate: (String) -> Unit,
    onEngineSelected: (SearchEngine) -> Unit,
    onOpenVideoLibrary: () -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showEngineSelector by remember { mutableStateOf(false) }
    val currentEngine = SearchEngineConfig.findById(selectedEngineId)
        ?: SearchEngineConfig.builtInEngines.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(120.dp))

        Text(
            text = "MiniBrowser",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = currentEngine.name,
            color = AccentPurple,
            fontSize = 14.sp,
            modifier = Modifier.clickable { showEngineSelector = true }
        )

        Spacer(Modifier.height(32.dp))

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSubmit = {
                if (query.isNotBlank()) {
                    onNavigate(query.trim())
                }
            }
        )

        Spacer(Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onOpenVideoLibrary() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VideoLibrary, "视频库", tint = AccentPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("视频", color = AccentPurple, fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.clickable { onOpenBookmarks() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bookmark, "书签", tint = AccentPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("书签", color = AccentPurple, fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.clickable { onOpenHistory() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, "历史", tint = AccentPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("历史", color = AccentPurple, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SearchEngineConfig.builtInEngines) { engine ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onNavigate(engine.homeUrl)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = engine.name,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = engine.name,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (showEngineSelector) {
        SearchEngineSelector(
            selectedEngineId = selectedEngineId,
            onEngineSelected = { engine ->
                onEngineSelected(engine)
                showEngineSelector = false
            },
            onDismiss = { showEngineSelector = false }
        )
    }
}
