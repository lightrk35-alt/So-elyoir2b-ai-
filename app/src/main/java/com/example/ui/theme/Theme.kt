package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OledColorScheme = darkColorScheme(
    primary = AccentNeutral,
    onPrimary = PureBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentSubtle,
    onSecondary = PureBlack,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = TextSecondary,
    tertiary = AccentSubtle,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OledColorScheme,
        typography = Typography,
        content = content
    )
}

