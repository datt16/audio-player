package io.github.datt16.audioplayer.core.player

import android.net.Uri
import kotlinx.coroutines.flow.Flow

typealias PlaybackProgress = Pair<Float, Long> // (progressPercentage, durationSeconds)

interface PlaybackManager {
  val isPlaying: Boolean
  val duration: Long
  val playbackProgressFlow: Flow<PlaybackProgress>
  suspend fun setup(uri: Uri)
  suspend fun setup(mediaFileByteArray: ByteArray)
  fun play()
  fun pause()
  fun seekToByPercentage(playbackPercentage: Float)
}
