package io.github.datt16.audioplayer.screens.handson.playground

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import io.github.datt16.audioplayer.viewmodels.AudioPlayerPlaygroundViewModel

@UnstableApi
@Composable
fun AudioPlayerManagerPlaygroundScreen(
  onClickNavUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AudioPlayerPlaygroundViewModel = hiltViewModel(),
) {
  BackHandler {
    onClickNavUp()
  }

  LaunchedEffect(Unit) {
    viewModel.initialize()
  }

  Column(modifier = modifier) {
    ElevatedButton(
      onClick = {
        viewModel.prepare(
          contentId = "hands_on_top",
          contentUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
        )
      }
    ) {
      Text("準備")
    }
    ElevatedButton(
      onClick = viewModel::play
    ) {
      Text("再生")
    }
    ElevatedButton(
      onClick = viewModel::pause
    ) {
      Text("停止")
    }
  }
}
