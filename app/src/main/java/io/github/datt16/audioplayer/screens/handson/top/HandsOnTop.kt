package io.github.datt16.audioplayer.screens.handson.top

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.datt16.audioplayer.core.navigation.HandsOnTopDestinations
import io.github.datt16.audioplayer.screens.handson.playground.AudioPlayerManagerPlaygroundScreen

@Composable
fun HandsOnTop(
  modifier: Modifier = Modifier,
) {
  HandsOnTopNavHost(
    modifier = modifier,
  )
}

@OptIn(UnstableApi::class)
@Composable
fun HandsOnTopNavHost(modifier: Modifier = Modifier) {
  val navControllerHandsOnTopScoped = rememberNavController()
  NavHost(
    navController = navControllerHandsOnTopScoped,
    startDestination = HandsOnTopDestinations.HandsOnTop,
  ) {
    composable<HandsOnTopDestinations.HandsOnTop> {
      HandsOnTopActionListScreen(modifier, navControllerHandsOnTopScoped)
    }
    composable<HandsOnTopDestinations.AudioPlayerManagerPlayground> {
      AudioPlayerManagerPlaygroundScreen(
        modifier = modifier,
        onClickNavUp = navControllerHandsOnTopScoped::navigateUp
      )
    }
  }
}
