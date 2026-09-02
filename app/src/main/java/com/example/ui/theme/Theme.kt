package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsportsColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Slate950,
  primaryContainer = CyanSurface,
  onPrimaryContainer = CyanGlow,
  secondary = NeonCyanBright,
  onSecondary = Slate950,
  secondaryContainer = Slate800,
  onSecondaryContainer = Slate200,
  tertiary = StatusPurple,
  onTertiary = Color.White,
  background = Slate950,
  onBackground = Slate50,
  surface = Slate900,
  onSurface = Slate50,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate400,
  outline = Slate700,
  outlineVariant = Slate800,
  error = StatusRose,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = EsportsColorScheme,
    typography = Typography,
    content = content
  )
}

