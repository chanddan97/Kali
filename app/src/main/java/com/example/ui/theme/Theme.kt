package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioSurfaceHighlight,
    onPrimaryContainer = StudioVioletLight,
    secondary = StudioCyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = StudioCyanAccent,
    tertiary = StudioAmberEmotion,
    onTertiary = Color(0xFF452B00),
    background = StudioDarkBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF384358),
    outlineVariant = Color(0xFF283040)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B42D1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADBFF),
    onPrimaryContainer = Color(0xFF24005B),
    secondary = Color(0xFF006874),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9EEFFD),
    onSecondaryContainer = Color(0xFF001F24),
    tertiary = Color(0xFF865300),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF191C20),
    surface = Color.White,
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark-first studio interface as per PRD
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
