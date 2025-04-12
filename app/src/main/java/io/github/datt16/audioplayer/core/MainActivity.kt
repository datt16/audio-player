package io.github.datt16.audioplayer.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.core.navigation.AudioPlayerAppNavHost
import io.github.datt16.audioplayer.screens.common.AudioPlayerAppScaffold

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navHostController = rememberNavController()
      AudioPlayerAppTheme {
        AudioPlayerAppScaffold(
          navController = navHostController
        ) { paddingValues ->
          AudioPlayerAppNavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navHostController
          )
        }
      }
    }
  }
}
