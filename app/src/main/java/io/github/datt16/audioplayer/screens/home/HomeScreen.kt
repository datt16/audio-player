package io.github.datt16.audioplayer.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  var progressPercentage by remember { mutableFloatStateOf(0f) }
  var playbackIcon by remember { mutableStateOf(Icons.Default.PlayArrow) }

  val audioLevel by viewModel.audioLevelFlow.collectAsState(initial = 0f)

  LaunchedEffect(Unit) {
    viewModel.startPlayback("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
    playbackIcon = Icons.Outlined.PlayArrow
  }
  LaunchedEffect(Unit) {
    viewModel.playbackFlow.collect { (progress, _) -> progressPercentage = progress }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {

    // アバター写真と音量に反応するアニメーション
    AudioReactiveAvatar(
      audioLevel = audioLevel,
      modifier =
      Modifier
        .aspectRatio(1f) // 正円を維持するためにアスペクト比を1:1に固定
        .size(200.dp)
        .padding(vertical = 16.dp),
    )

    // 従来のオーディオビジュアライザー
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
        Icon(modifier = Modifier.size(24.dp), imageVector = playbackIcon, contentDescription = null)
      }
      Spacer(modifier = Modifier.width(4.dp))
      Slider(
        onValueChange = { progressPercentage = it },
        onValueChangeFinished = { viewModel.seekTo(progressPercentage) },
        modifier = Modifier.weight(1f),
        value = progressPercentage,
        valueRange = 0f..1f,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text =
        "$currentPositionMinutes".padStart(2, '0') +
          ":" +
          "$currentPositionSeconds".padStart(2, '0'),
        style = AudioPlayerAppTheme.typography.labelMedium,
      )
    }
  }
}

@Composable
fun AudioReactiveAvatar(
  audioLevel: Float,
  modifier: Modifier = Modifier,
  avatarSize: Float = 0.6f,
) {
  // プレースホルダーのサイズを基準値として定義
  val baseSizeDp = 100.dp
  // 内側の円の基準サイズ(1.5倍)
  val innerSizeDp = 150.dp
  // 外側の円の基準サイズ(1.8倍)
  val outerSizeDp = 180.dp

  // 最小サイズの比率（プレースホルダーより一回り大きいサイズ）
  // 内側の円の最小サイズ（プレースホルダーの1.15倍）
  val minInnerRatio = 1.15f
  // 外側の円の最小サイズ（プレースホルダーの1.25倍）
  val minOuterRatio = 1.25f

  // ゲインの閾値 - この値以下だと円が最小サイズになる
  val thresholdLevel = 0.05f

  // 実際のスケールを計算（閾値以下ならサイズを小さくする）
  val effectiveAudioLevel =
    if (audioLevel < thresholdLevel) {
      // 閾値以下の場合、円を最小サイズに縮小
      0f
    } else {
      // 閾値以上の場合は通常通りのレベル（ただし閾値分を引いて正規化）
      (audioLevel - thresholdLevel) / (1f - thresholdLevel)
    }

  // 外側の円のスケールアニメーション
  val outerScaleAnimation = remember { Animatable(1f) }
  // 内側の円のスケールアニメーション
  val innerScaleAnimation = remember { Animatable(1f) }

  // 外側の円のアニメーション
  LaunchedEffect(effectiveAudioLevel) {
    // 音量に応じたターゲットスケール
    // 音量が閾値以下の場合は最小サイズを適用（プレースホルダーの1.25倍）= minOuterRatio * baseSizeDp/outerSizeDp
    val minScale = minOuterRatio * baseSizeDp.value / outerSizeDp.value
    // 通常時の最大拡大率（0.5倍）
    val maxAdditionalScale = 0.5f

    val targetOuterScale = minScale + (effectiveAudioLevel * maxAdditionalScale)

    outerScaleAnimation.animateTo(
      targetValue = targetOuterScale,
      animationSpec = spring(dampingRatio = 0.6f, stiffness = 60f)
    )
  }

  // 内側の円のアニメーション
  LaunchedEffect(effectiveAudioLevel) {
    // 最小サイズを適用（プレースホルダーの1.15倍）= minInnerRatio * baseSizeDp/innerSizeDp
    val minScale = minInnerRatio * baseSizeDp.value / innerSizeDp.value
    // 通常時の最大拡大率（0.3倍）
    val maxAdditionalScale = 0.3f

    val targetInnerScale = minScale + (effectiveAudioLevel * maxAdditionalScale)

    innerScaleAnimation.animateTo(
      targetValue = targetInnerScale,
      animationSpec =
      spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
      )
    )
  }

  // 正円を維持するためにアスペクト比1:1のBoxを使用
  Box(
    modifier =
    modifier
      .aspectRatio(1f) // 正円を維持するためにアスペクト比を1:1に固定
      .size(200.dp), // サイズを固定して正円を確保
    contentAlignment = Alignment.Center
  ) {
    // 最も外側の円
    Surface(
      modifier = Modifier
        .size(outerSizeDp)
        .scale(outerScaleAnimation.value),
      color = AudioPlayerAppTheme.colors.primary.copy(alpha = 0.08f),
      shape = CircleShape
    ) {}

    // 内側の円
    Surface(
      modifier = Modifier
        .size(innerSizeDp)
        .scale(innerScaleAnimation.value),
      color = AudioPlayerAppTheme.colors.primary.copy(alpha = 0.15f),
      shape = CircleShape
    ) {}

    // 中央のアバター写真
    Surface(
      modifier =
      Modifier
        .size(baseSizeDp)
        .scale(1f + (audioLevel * 0.05f)) // わずかに拡大縮小
        .clip(CircleShape),
      color = AudioPlayerAppTheme.colors.primary
    ) {
      // ここに将来的に実際の写真を表示する予定
      Box(
        modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .background(AudioPlayerAppTheme.colors.primary)
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
    smoothedFrequencyMap =
      rawFrequencyMap
        .map { (frequency, currentMagnitude) ->
          val previousMagnitude = smoothedFrequencyMap[frequency] ?: currentMagnitude
          val newMagnitude = previousMagnitude * 0.8f + currentMagnitude * 0.2f
          frequency to newMagnitude
        }
        .toMap()
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
  AudioPlayerAppTheme { HomeScreen() }
}
