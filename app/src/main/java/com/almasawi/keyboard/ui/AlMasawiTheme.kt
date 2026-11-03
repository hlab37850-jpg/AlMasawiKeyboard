package com.almasawi.keyboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val Scheme = darkColorScheme(
    primary = Color(0xFF6E8BFF),
    onPrimary = Color(0xFF0B1026),
    secondary = Color(0xFF9B7BFF),
    background = Color(0xFF0B1026),
    onBackground = Color(0xFFEAEDFB),
    surface = Color(0xFF131A3A),
    onSurface = Color(0xFFEAEDFB),
    surfaceVariant = Color(0xFF1B2450),
    onSurfaceVariant = Color(0xFFB4BCE0),
)

/** App UI is Arabic-first, so the whole tree is forced to RTL. */
@Composable
fun AlMasawiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box { content() }
        }
    }
}
