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
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

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
      viewModel.audioFrequencyMapFlow,
      modifier = Modifier
        .height(200.dp)
        .fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      val duration = viewModel.duration
      val currentPositionMinutes = (duration * progressPercentage / 1000f / 60).toInt()
      val currentPositionSeconds = (duration * progressPercentage / 1000f % 60).toInt()

      IconButton(
        onClick = {
          if (viewModel.isPlaying) {
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
  frequencyMapFlow: Flow<Map<Float, Float>>,
  modifier: Modifier = Modifier,
  barColor: Color = AudioPlayerAppTheme.colors.primary,
) {
  val rawFrequencyMap by frequencyMapFlow.collectAsState(initial = emptyMap())
  // 低域通過フィルタを使った平滑化を適用するための状態
  var smoothedFrequencyMap by remember { mutableStateOf(rawFrequencyMap) }

  // 平滑化処理：ComposeのLaunchedEffect内で低頻度に更新
  LaunchedEffect(rawFrequencyMap) {
    // 過去の状態との補間処理
    smoothedFrequencyMap = rawFrequencyMap.map { (frequency, currentMagnitude) ->
      val previousMagnitude = smoothedFrequencyMap[frequency] ?: currentMagnitude
      val newMagnitude = previousMagnitude * 0.8f + currentMagnitude * 0.2f
      frequency to newMagnitude
    }.toMap()
  }

  Canvas(modifier = modifier) {
    if (smoothedFrequencyMap.isNotEmpty()) {
      val sortedEntries = smoothedFrequencyMap.toSortedMap()
      val maxMagnitude = sortedEntries.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
      val barWidth = size.width / sortedEntries.size
      sortedEntries.entries.forEachIndexed { index, entry ->
        val magnitude = entry.value
        val barHeight = (magnitude / maxMagnitude) * size.height

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
