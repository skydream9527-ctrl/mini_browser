package com.minibrowser.app.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.minibrowser.app.ui.theme.Black
import com.minibrowser.app.ui.theme.Toolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    var fontSize by remember { mutableIntStateOf(18) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "阅读模式",
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(onClick = { if (fontSize > 12) fontSize -= 2 }) {
                    Icon(Icons.Default.TextDecrease, "缩小字体", tint = TextSecondary)
                }
                IconButton(onClick = { if (fontSize < 32) fontSize += 2 }) {
                    Icon(Icons.Default.TextIncrease, "放大字体", tint = TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Toolbar)
        )

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#0D0D0D"))
                    settings.javaScriptEnabled = false
                    settings.defaultTextEncodingName = "UTF-8"
                }
            },
            update = { webView ->
                val html = buildReaderHtml(title, content, fontSize)
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun buildReaderHtml(title: String, content: String, fontSize: Int): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body {
                background: #0D0D0D;
                color: #F0F0F0;
                font-family: -apple-system, sans-serif;
                font-size: ${fontSize}px;
                line-height: 1.8;
                padding: 16px;
                word-wrap: break-word;
            }
            h1 {
                font-size: ${fontSize + 6}px;
                color: #F0F0F0;
                margin-bottom: 16px;
                line-height: 1.4;
                border-bottom: 1px solid #141414;
                padding-bottom: 12px;
            }
            p { margin-bottom: 16px; }
            img {
                max-width: 100%;
                height: auto;
                border-radius: 8px;
                margin: 12px 0;
            }
            a { color: #00E5FF; text-decoration: none; }
            blockquote {
                border-left: 3px solid #7C4DFF;
                padding-left: 16px;
                margin: 16px 0;
                color: #666666;
            }
            pre, code {
                background: #1A1A1A;
                padding: 2px 6px;
                border-radius: 4px;
                font-size: ${fontSize - 2}px;
            }
            pre { padding: 12px; overflow-x: auto; }
            ul, ol { padding-left: 24px; margin-bottom: 16px; }
            li { margin-bottom: 4px; }
            h2, h3, h4 {
                color: #F0F0F0;
                margin: 20px 0 12px;
            }
            table { border-collapse: collapse; width: 100%; margin: 12px 0; }
            td, th { border: 1px solid #141414; padding: 8px; }
        </style>
        </head>
        <body>
            <h1>${title.replace("<", "&lt;").replace(">", "&gt;")}</h1>
            $content
        </body>
        </html>
    """.trimIndent()
}
