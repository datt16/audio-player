package io.github.datt16.abstraction.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.datt16.abstraction.screens.home.HomeScreen

@Composable
fun AbstractionAppNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
  NavHost(
    navController = navController,
    startDestination = AbstractionAppDestinations.Home
  ) {
    composable<AbstractionAppDestinations.Home> {
      HomeScreen(
        modifier = modifier,
        sampleKey = "datt11"
      )
    }
  }
}
