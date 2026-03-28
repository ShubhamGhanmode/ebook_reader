package com.shubhamghanmode.inkfold.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NightAccent,
    onPrimary = NightPaper,
    primaryContainer = Chestnut,
    onPrimaryContainer = Parchment,
    secondary = Wheat,
    onSecondary = NightPaper,
    secondaryContainer = Color(0xFF3B2D24),
    onSecondaryContainer = Parchment,
    tertiary = Moss,
    onTertiary = NightPaper,
    tertiaryContainer = Color(0xFF2D3225),
    onTertiaryContainer = Parchment,
    background = NightPaper,
    onBackground = Color(0xFFF6E7D2),
    surface = NightSurface,
    onSurface = Color(0xFFF6E7D2),
    onSurfaceVariant = Color(0xFFD7C1AA)
)

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color(0xFFFFF8F2),
    primaryContainer = ParchmentDeep,
    onPrimaryContainer = Ink,
    secondary = Moss,
    onSecondary = Color(0xFFFFFCF8),
    secondaryContainer = Color(0xFFE7E4D1),
    onSecondaryContainer = Ink,
    tertiary = Chestnut,
    onTertiary = Color(0xFFFFF8F2),
    tertiaryContainer = Color(0xFFF0D5C4),
    onTertiaryContainer = Ink,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentMuted,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDDCC4),
    onSurfaceVariant = InkSoft
)

@Composable
fun InkFoldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
