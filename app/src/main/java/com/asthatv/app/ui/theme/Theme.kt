package com.asthatv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AsthaDark = darkColorScheme(
    background = Color.Black,
    surface = Color(0xFF08090B),
    surfaceVariant = Color(0xFF111216),
    primary = Color.White,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AsthaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AsthaDark,
        content = content
    )
}
