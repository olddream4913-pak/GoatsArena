package com.clouddevicemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = AuroraBlue,
    onPrimary = CloudWhite,
    secondary = CircuitTeal,
    tertiary = PulseViolet,
    background = InkBlack,
    onBackground = CloudWhite,
    surface = DeepSlate,
    onSurface = CloudWhite,
    onSurfaceVariant = MistGray
)

private val LightScheme = lightColorScheme(
    primary = AuroraBlue,
    onPrimary = CloudWhite,
    secondary = CircuitTeal,
    tertiary = PulseViolet,
    background = CloudWhite,
    onBackground = InkBlack,
    surface = Color(0xFFEFF3FF),
    onSurface = InkBlack,
    onSurfaceVariant = Color(0xFF556080)
)

@Composable
fun CloudDeviceManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkScheme else LightScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}