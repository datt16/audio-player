package io.github.datt16.audioplayer.core.visualizer

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.hypot

class AudioVisualizer(
  audioSessionId: Int
) {
  // TODO: インスタンス化時にパーミッションリクエストで例外が返る可能性があるので、audioVisualizerをnullableにする
  private val frequencyMapMutableSharedFlow = MutableSharedFlow<Map<Float, Float>>(replay = 1)
  val frequencyMapFlow: SharedFlow<Map<Float, Float>> = frequencyMapMutableSharedFlow

  private val audioVisualizer: Visualizer = Visualizer(audioSessionId).apply {
    enabled = true
    captureSize = Visualizer.getCaptureSizeRange()[1] // 最大キャプチャサイズ
    setDataCaptureListener(
      object : Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
          // Noop
        }

        override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray?, samplingRate: Int) {
          // FFTデータ 2バイトずつペアで実部と虚部が保存されている
          fft?.let {
            val frequencyMap = mutableMapOf<Float, Float>()
            for (i in 2 until it.size step 2) {
              val re = it[i].toInt()
              val im = it[i + 1].toInt()
              // 振幅を計算（ゲイン）
              val magnitude = hypot(re.toDouble(), im.toDouble()).toFloat()
              // 周波数はサンプリングレートやFFTサイズから計算できますが、ここでは単純なインデックスをキーにする例です
              val frequency = i / 2f
              frequencyMap[frequency] = magnitude
            }
            frequencyMapMutableSharedFlow.tryEmit(frequencyMap)
          }
        }
      },
      Visualizer.getMaxCaptureRate() / 2,
      false,
      true
    )
  }


  fun release() {
    audioVisualizer.release()
  }
}