package com.minibrowser.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserView(
    session: GeckoSession,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            GeckoView(ctx).apply {
                setSession(session)
            }
        },
        modifier = modifier
    )
}
