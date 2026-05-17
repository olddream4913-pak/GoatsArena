package com.goatsarena.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ArenaPrimary,
    onPrimary = ArenaOnPrimary,
    primaryContainer = ArenaPrimaryContainer,
    secondary = ArenaSecondary,
    background = ArenaBackground,
    surface = ArenaSurface,
    onSurface = ArenaOnSurface,
    error = ArenaError
)

private val DarkColors = darkColorScheme(
    primary = ArenaPrimaryContainer,
    onPrimary = ArenaOnPrimary,
    secondary = ArenaSecondary,
    background = ArenaOnSurface,
    surface = ArenaOnSurface,
    onSurface = ArenaSurface,
    error = ArenaError
)

@Composable
fun GoatsArenaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}