package io.github.datt16.audioplayer.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  LaunchedEffect(Unit) {
    viewModel.startPlayback("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
  }
}

@Preview
@Composable
private fun HomeScreenPreview() {
  AudioPlayerAppTheme {
    HomeScreen()
  }
}
