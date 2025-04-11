package io.github.datt16.audioplayer.core.visualizer

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.hypot

private const val MIN_AUDIBLE_FREQUENCY = 20f
private const val MAX_AUDIBLE_FREQUENCY = 20000f

class AudioVisualizer(
  audioSessionId: Int,
) {
  private val frequencyMapMutableSharedFlow = MutableSharedFlow<Map<Float, Float>>(replay = 1)
  val frequencyMapFlow: SharedFlow<Map<Float, Float>> = frequencyMapMutableSharedFlow

  private val visualizer: Visualizer =
    Visualizer(audioSessionId).apply {
      captureSize =
        (
          Visualizer.getCaptureSizeRange().max() +
            Visualizer.getCaptureSizeRange().min()
          ) / 2
      setDataCaptureListener(
        object : Visualizer.OnDataCaptureListener {
          override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
            // Noop
          }

          override fun onFftDataCapture(
            visualizer: Visualizer,
            fft: ByteArray?,
            samplingRate: Int,
          ) {
            fft?.let { fftData ->
              val samplingRateHz = samplingRate / 1000f
              val numBins = fftData.size / 2
              val nyquistFrequency = samplingRateHz / 2f
              val frequencyResolution = nyquistFrequency / numBins.toFloat()

              val frequencyMap = mutableMapOf<Float, Float>()
              for (i in 2 until fftData.size step 2) {
                val binIndex = i / 2
                val frequencyHz = binIndex * frequencyResolution
                if (frequencyHz in MIN_AUDIBLE_FREQUENCY..MAX_AUDIBLE_FREQUENCY) {
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
    visualizer.setEnabled(true)
  }

  fun release() {
    visualizer.release()
  }
}
