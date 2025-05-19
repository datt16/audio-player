package io.github.datt16.audioplayer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.datt16.audioplayer.screens.handson.top.HandsOnTop
import io.github.datt16.audioplayer.screens.home.HomeScreen
import io.github.datt16.audioplayer.screens.settings.GlobalSettingsScreen

@Composable
fun AudioPlayerAppNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
  NavHost(
    navController = navController,
    startDestination = AudioPlayerAppDestinations.Home
  ) {
    composable<AudioPlayerAppDestinations.Home> {
      HomeScreen(
        modifier = modifier,
      )
    }
    composable<AudioPlayerAppDestinations.GlobalSettings> {
      GlobalSettingsScreen(
        modifier = modifier,
      )
    }
    composable<AudioPlayerAppDestinations.HandsOnTop> {
      HandsOnTop(
        modifier = modifier,
      )
    }
  }
}
