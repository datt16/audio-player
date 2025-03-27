package io.github.datt16.abstraction.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private const val LIGHT_PRIMARY_COLOR = 0xFF6200EE
private const val LIGHT_SECONDARY_COLOR = 0xFF03DAC6
private const val DARK_PRIMARY_COLOR = 0xFFBB86FC
private const val DARK_SECONDARY_COLOR = 0xFF03DAC6

val LightColorScheme = lightColorScheme(
  primary = Color(LIGHT_PRIMARY_COLOR),
  secondary = Color(LIGHT_SECONDARY_COLOR)
)

val DarkColorScheme = darkColorScheme(
  primary = Color(DARK_PRIMARY_COLOR),
  secondary = Color(DARK_SECONDARY_COLOR)
)
