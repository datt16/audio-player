package io.github.datt16.audioplayer.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
  var progressPercentage by remember { mutableFloatStateOf(0f) }
  var playbackIcon by remember { mutableStateOf(Icons.Default.PlayArrow) }

  LaunchedEffect(Unit) {
    viewModel.startPlayback("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
    playbackIcon = Icons.Outlined.PlayArrow
  }
  LaunchedEffect(Unit) {
    viewModel.playbackFlow.collect { (progress, _) ->
      progressPercentage = progress
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    AudioVisualizer(
      viewModel = viewModel,
      modifier = Modifier
        .height(200.dp)
        .fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      val duration = viewModel.getExoPlayer().duration
      val currentPositionMinutes = (duration * progressPercentage / 1000f / 60).toInt()
      val currentPositionSeconds = (duration * progressPercentage / 1000f % 60).toInt()

      IconButton(
        onClick = {
          if (viewModel.getExoPlayer().isPlaying) {
            viewModel.pause()
            playbackIcon = Icons.Default.PlayArrow
          } else {
            playbackIcon = Icons.Outlined.PlayArrow
            viewModel.play()
          }
        }
      ) {
        Icon(
          modifier = Modifier.size(24.dp),
          imageVector = playbackIcon,
          contentDescription = null
        )
      }
      Spacer(modifier = Modifier.width(4.dp))
      Slider(
        onValueChange = {
          progressPercentage = it
        },
        onValueChangeFinished = {
          viewModel.seekTo(progressPercentage)
        },
        modifier = Modifier.weight(1f),
        value = progressPercentage,
        valueRange = 0f..1f,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "$currentPositionMinutes".padStart(2, '0')
          + ":"
          + "$currentPositionSeconds".padStart(2, '0'),
        style = AudioPlayerAppTheme.typography.labelMedium,
      )
    }
  }
}

@Composable
fun AudioVisualizer(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier,
  barColor: Color = AudioPlayerAppTheme.colors.primary,
) {
  val frequencyMap by viewModel.audioFrequencyMapFlow.collectAsState(initial = emptyMap())
  Canvas(modifier = modifier) {
    if (frequencyMap.isNotEmpty()) {
      // 周波数キーの昇順にソート
      val sortedEntries = frequencyMap.toSortedMap()
      // 振幅の最大値を取得（ゼロ割防止のため最低1f）
      val maxMagnitude = sortedEntries.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
      // バーの幅を計算（エントリ数に合わせてキャンバス全体に広げる）
      val barWidth = size.width / sortedEntries.size

      sortedEntries.entries.forEachIndexed { index, entry ->
        val magnitude = entry.value
        // 正規化された高さ（キャンバスの高さに対しての割合）
        val barHeight = (magnitude / maxMagnitude) * size.height

        // キャンバスの下からバーを描画
        drawRect(
          color = barColor,
          topLeft = Offset(x = index * barWidth, y = size.height - barHeight),
          size = Size(width = barWidth, height = barHeight)
        )
      }
    }
  }
}

@Preview
@Composable
private fun HomeScreenPreview() {
  AudioPlayerAppTheme {
    HomeScreen()
  }
}
