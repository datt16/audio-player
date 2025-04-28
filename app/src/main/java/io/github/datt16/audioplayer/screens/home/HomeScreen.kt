package io.github.datt16.audioplayer.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.viewmodels.HomeViewModel
import androidx.compose.animation.core.Spring as composeSpring
import androidx.compose.animation.core.spring as composeSpring

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val audioLevel by viewModel.audioLevelFlow.collectAsState(initial = 0f)
  val playbackProgress by viewModel.playbackFlow.collectAsState(initial = null)

  LaunchedEffect(Unit) { viewModel.fetchMediaFiles() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(top = 16.dp)
  ) {
    // 固定部分（プログレスバーまで）
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        AudioReactiveAvatar(audioLevel = audioLevel, modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(16.dp))
        PlaybackController(
          progressPercentage = playbackProgress?.first ?: 0f,
          playbackIcon = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
          isPlaying = isPlaying,
          duration = viewModel.duration,
          onClickPlay = viewModel::play,
          onClickPause = viewModel::pause,
          onSeekChange = viewModel::seekTo,
        )
      }
    }

    // スクロール可能なメディアファイルリスト部分
    when (uiState) {
      is HomeUiState.Loading -> {
        LoadingScreen(modifier = Modifier.fillMaxSize())
      }

      is HomeUiState.Success -> {
        MediaFileList(
          mediaFiles = (uiState as HomeUiState.Success).mediaFiles,
          onClickMediaItem = viewModel::startPlayback,
          modifier = Modifier.fillMaxSize()
        )
      }

      is HomeUiState.Error -> {
        ErrorScreen(
          message = (uiState as HomeUiState.Error).message,
          onRetry = viewModel::fetchMediaFiles,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
private fun PlaybackController(
  isPlaying: Boolean,
  duration: Long,
  progressPercentage: Float,
  playbackIcon: ImageVector,
  onClickPlay: () -> Unit,
  onClickPause: () -> Unit,
  onSeekChange: (progressPercentage: Float) -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    val currentPositionMinutes = (duration * progressPercentage / 1000f / 60).toInt()
    val currentPositionSeconds = (duration * progressPercentage / 1000f % 60).toInt()

    IconButton(
      onClick = {
        if (isPlaying) {
          onClickPause()
        } else {
          onClickPlay()
        }
      }
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        imageVector = playbackIcon,
        contentDescription = if (isPlaying) "一時停止" else "再生"
      )
    }
    Spacer(modifier = Modifier.width(4.dp))
    Slider(
      onValueChange = onSeekChange,
      onValueChangeFinished = { onSeekChange(progressPercentage) },
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

@Composable
fun AudioReactiveAvatar(
  audioLevel: Float,
  modifier: Modifier = Modifier,
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
      animationSpec = composeSpring(dampingRatio = 0.6f, stiffness = 60f)
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
      composeSpring(
        dampingRatio = composeSpring.DampingRatioLowBouncy,
        stiffness = composeSpring.StiffnessLow
      )
    )
  }

  // 正円を維持するためにアスペクト比1:1のBoxを使用
  Box(
    modifier = modifier.aspectRatio(1f), // 正円を維持するためにアスペクト比を1:1に固定
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
fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = message)
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onRetry) { Text("再試行") }
    }
  }
}

@Preview
@Composable
private fun HomeScreenPreview() {
  AudioPlayerAppTheme { Surface { HomeScreen() } }
}
