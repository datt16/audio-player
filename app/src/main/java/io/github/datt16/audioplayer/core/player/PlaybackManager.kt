package io.github.datt16.audioplayer.core.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

typealias PlaybackProgress = Pair<Float, Long> // (progressPercentage, durationSeconds)

enum class PlaybackState {
  PLAYING,
  PAUSE,
}

@Singleton
class PlaybackManager @Inject constructor(
  val exoPlayer: ExoPlayer,
  private val dataSourceFactory: DataSource.Factory,
) {

  @OptIn(UnstableApi::class)
  fun setup(uri: Uri) {
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
  }

  fun play() {
    exoPlayer.play()
  }

  fun pause() {
    exoPlayer.pause()
  }

  fun seekToByPercentage(playbackPercentage: Float) {
    exoPlayer.seekTo((exoPlayer.duration * playbackPercentage).toLong())
  }

  fun getPlaybackProgressFlow(pollInterval: Long = 1000L): Flow<PlaybackProgress> = flow {
    while (true) {
      val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
      val currentPosition = exoPlayer.currentPosition
      val progressPercentage = currentPosition.toFloat() / duration
      emit(Pair(progressPercentage, currentPosition))
      delay(pollInterval)
    }
  }
}
