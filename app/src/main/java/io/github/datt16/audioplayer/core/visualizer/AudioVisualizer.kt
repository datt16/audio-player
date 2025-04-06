package io.github.datt16.audioplayer.core.visualizer

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import kotlin.math.hypot

class AudioVisualizer(
  audioSessionId: Int
) {
  // TODO: インスタンス化時にパーミッションリクエストで例外が返る可能性があるので、audioVisualizerをnullableにする
  private val frequencyMapMutableSharedFlow = MutableSharedFlow<Map<Float, Float>>(replay = 1)
  val frequencyMapFlow: SharedFlow<Map<Float, Float>> = frequencyMapMutableSharedFlow

  private val audioVisualizer: Visualizer = Visualizer(audioSessionId).apply {
    captureSize = (Visualizer.getCaptureSizeRange().max() + Visualizer.getCaptureSizeRange().min()) / 2
    setDataCaptureListener(
      object : Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
          // Noop
        }

        override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray?, samplingRate: Int) {
          fft?.let { fftData ->
            // samplingRate は mHz 単位と仮定して変換（例: 48000000 mHz -> 48000 Hz）
            val samplingRateHz = samplingRate / 1000f

            // fftData.size はバイト数なので、実際の FFT ビン数は fftData.size / 2 になります
            val numBins = fftData.size / 2
            // Nyquist 周波数は samplingRateHz / 2
            val nyquistFrequency = samplingRateHz / 2f
            // 周波数分解能は Nyquist をビン数で割る
            val frequencyResolution = nyquistFrequency / numBins.toFloat()

            val frequencyMap = mutableMapOf<Float, Float>()
            // 最初の2バイト（DC成分）をスキップして、2バイトずつ処理
            for (i in 2 until fftData.size step 2) {
              val binIndex = i / 2
              // 各ビンの実際の周波数 (Hz)
              val frequencyHz = binIndex * frequencyResolution
              // 可聴域 (20Hz～20kHz) に絞る例
              if (frequencyHz in 20f..20000f) {
                val re = fftData[i].toInt()
                val im = fftData[i + 1].toInt()
                val magnitude = hypot(re.toDouble(), im.toDouble()).toFloat()
                frequencyMap[frequencyHz] = magnitude
              }
            }
            frequencyMapMutableSharedFlow.tryEmit(frequencyMap)
          }
        }
      },
      Visualizer.getMaxCaptureRate(),
      false,
      true
    )
  }

  fun enable() {
    audioVisualizer.setEnabled(true)
  }

  fun release() {
    audioVisualizer.release()
  }
}