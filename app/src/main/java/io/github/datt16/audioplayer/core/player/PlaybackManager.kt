package io.github.datt16.audioplayer.core.player

import android.net.Uri
import io.github.datt16.audioplayer.core.visualizer.AudioVisualizer
import kotlinx.coroutines.flow.Flow

typealias PlaybackProgress = Pair<Float, Long> // (progressPercentage, durationSeconds)

interface PlaybackManager {
  val audioVisualizer: AudioVisualizer?
  val isPlaying: Boolean
  val duration: Long
  val playbackProgressFlow: Flow<PlaybackProgress>
  suspend fun setup(uri: Uri)
  fun play()
  fun pause()
  fun seekToByPercentage(playbackPercentage: Float)
}
