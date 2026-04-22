package com.minibrowser.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

data class ContextMenuTarget(
    val url: String,
    val title: String = "",
    val isImage: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebContextMenu(
    target: ContextMenuTarget,
    onOpenInNewTab: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = target.url,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            ContextMenuItem(
                icon = Icons.Default.OpenInNew,
                text = "在新标签页中打开",
                onClick = {
                    onOpenInNewTab(target.url)
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.ContentCopy,
                text = "复制链接",
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("url", target.url))
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Share,
                text = "分享链接",
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, target.url)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                    onDismiss()
                }
            )

            if (target.isImage) {
                ContextMenuItem(
                    icon = Icons.Default.Download,
                    text = "保存图片",
                    onClick = {
                        onDownload(target.url)
                        onDismiss()
                    }
                )
            }

            ContextMenuItem(
                icon = Icons.Default.Download,
                text = if (target.isImage) "下载链接" else "下载",
                onClick = {
                    onDownload(target.url)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, color = TextPrimary, fontSize = 15.sp)
    }
}
