package com.minibrowser.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BlackColorScheme = darkColorScheme(
    primary = Purple,
    secondary = Red,
    background = Black,
    surface = Surface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Toolbar,
    onSurfaceVariant = TextSecondary,
    outline = Divider
)

@Composable
fun MiniBrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlackColorScheme,
        typography = MiniBrowserTypography,
        content = content
    )
}
