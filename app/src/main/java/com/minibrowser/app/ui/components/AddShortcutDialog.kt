package com.minibrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun AddShortcutDialog(
    initialTitle: String = "",
    initialUrl: String = "",
    onConfirm: (title: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var url by remember { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text("添加快捷方式", color = TextPrimary, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            Text("名称", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkToolbar, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text("网址", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = url,
                onValueChange = { url = it },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkToolbar, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                decorationBox = { inner ->
                    if (url.isEmpty()) {
                        Text("https://", color = TextSecondary, fontSize = 14.sp)
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onConfirm(title.trim(), url.trim())
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("添加", color = TextPrimary)
            }
        }
    }
}
