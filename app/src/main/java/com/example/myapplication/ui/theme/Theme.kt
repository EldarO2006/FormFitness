package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Yellow40,
    secondary = Yellow80,
    tertiary = White40,
    background = Black,
    surface = Black80,
    surfaceVariant = Black40,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White
)

private val LightColorScheme = lightColorScheme(
    primary = Yellow40,
    secondary = Yellow80,
    tertiary = Black40,
    background = White,
    surface = White80,
    surfaceVariant = White40,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
    onSurfaceVariant = Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}