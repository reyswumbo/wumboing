package com.wumboing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wumboing.app.R

// Cutecore pastel palette (soft pinks & lavenders)
val BubblePink = Color(0xFFFF7B9C)
val BubblePinkDark = Color(0xFFE0577E)
val Lavender = Color(0xFFC3AED6)
val LavenderBlush = Color(0xFFFFF0F5)
val Blush = Color(0xFFFDE3EC)
val Plum = Color(0xFF4A2C4A)
val SoftPlum = Color(0xFF8A6A80)
val NightPlum = Color(0xFF2A1B33)
val CardPlum = Color(0xFF3A2A45)
val DeepBlush = Color(0xFF4A3457)

private val DarkScheme = darkColorScheme(
    primary = BubblePink,
    onPrimary = Color(0xFF4A0030),
    primaryContainer = Color(0xFF5C1E3F),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Lavender,
    onSecondary = Color(0xFF3C2A4A),
    background = NightPlum,
    onBackground = Color(0xFFF7E8F0),
    surface = CardPlum,
    onSurface = Color(0xFFF7E8F0),
    surfaceVariant = DeepBlush,
    onSurfaceVariant = Color(0xFFD8C4D0),
    surfaceContainer = CardPlum,
    surfaceContainerHigh = Color(0xFF4A3457),
    outline = Color(0xFF805E73)
)

private val LightScheme = lightColorScheme(
    primary = BubblePinkDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3B0A26),
    secondary = Color(0xFF9A7BB8),
    onSecondary = Color.White,
    background = LavenderBlush,
    onBackground = Plum,
    surface = Color.White,
    onSurface = Plum,
    surfaceVariant = Blush,
    onSurfaceVariant = SoftPlum,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFFBE7EF),
    outline = Color(0xFFC4A8B2)
)

private val CuteFont: FontFamily = FontFamily(Font(R.font.fredoka_family))

private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = CuteFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp)
)

@Composable
fun WumboingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AppTypography,
        content = content
    )
}

val WumboingTypography = AppTypography
