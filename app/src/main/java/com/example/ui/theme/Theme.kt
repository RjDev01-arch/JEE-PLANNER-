package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = DarkBg,
    primaryContainer = DarkSurfaceHighlight,
    onPrimaryContainer = CyanNeon,
    secondary = IndigoGlow,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceHighlight,
    onSecondaryContainer = IndigoGlow,
    tertiary = AmberGold,
    onTertiary = DarkBg,
    tertiaryContainer = DarkSurfaceHighlight,
    onTertiaryContainer = AmberGold,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = RoseDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent futuristic dark academic branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
