package io.github.datt16.audioplayer.core.player

import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore.Audio
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.datt16.audioplayer.core.visualizer.AudioVisualizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

typealias PlaybackProgress = Pair<Float, Long> // (progressPercentage, durationSeconds)

enum class PlaybackState {
  PLAYING,
  PAUSE,
}

@UnstableApi
@Singleton
class PlaybackManager @Inject constructor(
  private val exoPlayer: ExoPlayer,
  private val dataSourceFactory: DataSource.Factory,
) : Player.Listener {

  private var _audioVisualizer: AudioVisualizer? = null
  val audioVisualizer get() = _audioVisualizer

  @OptIn(UnstableApi::class)
  fun setup(uri: Uri) {
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()

    _audioVisualizer = initializeAudioVisualizer()
  }

  private fun initializeAudioVisualizer(): AudioVisualizer? {
    return try {
      AudioVisualizer(0)
    } catch (e: Exception) {
      Timber.tag("AudioVisualizer").e(e)
      null
    }
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

  fun getIsPlaying(): Boolean {
    return exoPlayer.isPlaying
  }

  fun getDuration(): Long {
    return exoPlayer.duration
  }
}
