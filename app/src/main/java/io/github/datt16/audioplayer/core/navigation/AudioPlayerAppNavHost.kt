package io.github.datt16.audioplayer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.datt16.audioplayer.screens.home.HomeScreen

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
  }
}
