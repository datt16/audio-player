package io.github.datt16.audioplayer.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

val LocalAppColors = compositionLocalOf { LightColorScheme }
val LocalAppTypography = compositionLocalOf { Typography() }

@Composable
fun AudioPlayerAppTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  CompositionLocalProvider(
    LocalAppColors provides colorScheme,
    LocalAppTypography provides Typography(),
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography(),
      content = content
    )
  }
}

object AudioPlayerAppTheme {
  val colors: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTypography.current
}
