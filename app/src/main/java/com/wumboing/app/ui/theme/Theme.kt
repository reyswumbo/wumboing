package com.wumboing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Crimson = Color(0xFFE94560)
val DeepNavy = Color(0xFF16213E)
val Midnight = Color(0xFF0F0F23)
val Slate = Color(0xFF1A1A2E)
val OffWhite = Color(0xFFF5F5F7)

private val DarkScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = Color.White,
    secondary = Color(0xFF8D99AE),
    background = Midnight,
    onBackground = OffWhite,
    surface = Slate,
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF23233F),
    onSurfaceVariant = Color(0xFFB8B8CE),
    surfaceContainer = Slate,
    surfaceContainerHigh = Color(0xFF24243F)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFFC2185B),
    onPrimary = Color.White,
    secondary = Color(0xFF5C6B7A),
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFECEAF3),
    onSurfaceVariant = Color(0xFF4A4A5A)
)

@Composable
fun WumboingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = WumboingTypography,
        content = content
    )
}

val WumboingTypography = androidx.compose.material3.Typography()
