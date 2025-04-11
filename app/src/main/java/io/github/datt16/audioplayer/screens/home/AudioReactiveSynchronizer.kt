package io.github.datt16.audioplayer.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 音声レベルの変化をアニメーションと同期させるためのユーティリティ フレームレート制限により、過剰な更新を防ぎつつスムーズなアニメーションを実現します */
@Composable
fun rememberSynchronizedAudioLevel(
  audioLevelFlow: Flow<Float>,
  initialValue: Float = 0f,
  animationSpec: AnimationSpec<Float> =
    spring(
      dampingRatio = Spring.DampingRatioLowBouncy,
      stiffness = Spring.StiffnessLow
    ),
  minFps: Float = 30f, // 最低フレームレート
): State<Float> {
  // 現在の音量レベル
  val audioLevelState = remember { mutableFloatStateOf(initialValue) }

  // アニメーション対象の値（スムーズに変化）
  val animatedLevel = remember { Animatable(initialValue) }

  // フレーム間の最小時間（ミリ秒）- フレームレート制限のため
  val minFrameTimeMs = remember { (1000f / minFps).toLong() }

  LaunchedEffect(audioLevelFlow) {
    var lastUpdateTimeMs = 0L

    // 音量レベルを取得
    launch { audioLevelFlow.collect { newLevel -> audioLevelState.floatValue = newLevel } }

    // フレームレートを制限しながらアニメーション更新
    launch {
      while (isActive) {
        val currentTimeMs = System.currentTimeMillis()

        // 前回の更新から最小フレーム時間が経過していれば更新
        if (currentTimeMs - lastUpdateTimeMs >= minFrameTimeMs) {
          animatedLevel.animateTo(
            targetValue = audioLevelState.floatValue,
            animationSpec = animationSpec
          )
          lastUpdateTimeMs = currentTimeMs
        }

        // Composeのフレームレートに合わせて更新
        withFrameNanos {}
      }
    }
  }

  // アニメーション値を返す
  return remember { derivedStateOf { animatedLevel.value } }
}

/** 周波数マップのアニメーション同期 複雑なデータ構造（周波数マップ）に対するスムーズな更新を提供 */
@Composable
fun rememberSynchronizedFrequencyMap(
  frequencyMapFlow: Flow<Map<Float, Float>>,
  minFps: Float = 30f,
): State<Map<Float, Float>> {
  // 現在の周波数マップ
  var currentMap by remember { mutableStateOf<Map<Float, Float>>(emptyMap()) }

  // スムーズ化された周波数マップ（前の値との補間）
  val smoothedMap = remember { mutableStateOf<Map<Float, Float>>(emptyMap()) }

  // フレーム間の最小時間（ミリ秒）
  val minFrameTimeMs = remember { (1000f / minFps).toLong() }

  LaunchedEffect(frequencyMapFlow) {
    var lastUpdateTimeMs = 0L

    launch { frequencyMapFlow.collect { newMap -> currentMap = newMap } }

    launch {
      while (isActive) {
        val currentTimeMs = System.currentTimeMillis()

        if (currentTimeMs - lastUpdateTimeMs >= minFrameTimeMs) {
          // 前の値と現在の値を補間（スムージング）
          val interpolatedMap =
            currentMap
              .map { (frequency, magnitude) ->
                val previousMagnitude = smoothedMap.value[frequency] ?: magnitude
                // 80%は前の値、20%は新しい値を使用した平滑化
                val smoothedMagnitude = previousMagnitude * 0.8f + magnitude * 0.2f
                frequency to smoothedMagnitude
              }
              .toMap()

          smoothedMap.value = interpolatedMap
          lastUpdateTimeMs = currentTimeMs
        }

        // Composeのフレームレートに合わせて更新
        withFrameNanos {}
      }
    }
  }

  return smoothedMap
}
