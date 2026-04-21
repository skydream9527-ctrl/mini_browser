package com.minibrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun FindInPageBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkToolbar)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(TextPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onNext() }),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(DarkToolbar.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("在页面中查找", color = TextSecondary, fontSize = 14.sp)
                }
                inner()
            }
        )
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.KeyboardArrowUp, "上一个", tint = TextPrimary)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowDown, "下一个", tint = TextPrimary)
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, "关闭", tint = TextSecondary)
        }
    }
}
