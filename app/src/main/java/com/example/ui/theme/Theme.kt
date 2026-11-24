package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val ProtonColorScheme = darkColorScheme(
    primary = ProtonPrimary,
    secondary = ProtonSecondary,
    background = ProtonBackground,
    surface = ProtonSurface,
    onPrimary = ProtonOnPrimary,
    onSecondary = ProtonOnPrimary,
    onBackground = ProtonOnSurface,
    onSurface = ProtonOnSurface,
    onSurfaceVariant = ProtonTextSecondary,
    surfaceVariant = ProtonSurface,
    error = ProtonError
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Force custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = ProtonColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
