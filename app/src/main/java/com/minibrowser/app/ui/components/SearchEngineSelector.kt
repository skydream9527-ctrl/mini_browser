package com.minibrowser.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchEngineSelector(
    selectedEngineId: String,
    onEngineSelected: (SearchEngine) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "选择搜索引擎",
                color = TextPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            SearchEngineConfig.builtInEngines.forEach { engine ->
                val isSelected = engine.id == selectedEngineId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEngineSelected(engine) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isSelected) AccentPurple else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = engine.name,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
