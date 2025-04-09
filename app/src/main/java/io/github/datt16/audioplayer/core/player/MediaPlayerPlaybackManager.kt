package io.github.datt16.audioplayer.core.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import io.github.datt16.audioplayer.core.visualizer.AudioVisualizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
class MediaPlayerPlaybackManager @Inject constructor(
  private val context: Context,
) : Player.Listener, PlaybackManager {

  private var _mediaPlayer: MediaPlayer? = null
  private var _audioVisualizer: AudioVisualizer? = null
  override val audioVisualizer get() = _audioVisualizer

  override val isPlaying: Boolean
    get() = _mediaPlayer?.isPlaying ?: false

  override val duration: Long
    get() = _mediaPlayer?.duration?.toLong() ?: 0L

  override val playbackProgressFlow: Flow<PlaybackProgress> = flow {
    val mediaPlayer = _mediaPlayer ?: return@flow

    while (true) {
      val duration = mediaPlayer.duration.toLong().takeIf { it > 0 } ?: 1L
      val currentPosition = mediaPlayer.currentPosition.toLong()
      val progressPercentage = currentPosition.toFloat() / duration
      emit(Pair(progressPercentage, currentPosition))
      delay(1000L)
    }
  }

  override suspend fun setup(uri: Uri) {
    val player = MediaPlayer.create(context, uri)
    val audioVisualizer = initializeAudioVisualizer(player.audioSessionId)

    audioVisualizer?.enable()

    _audioVisualizer = audioVisualizer
    _mediaPlayer = player
  }

  override fun play() {
    val player = _mediaPlayer ?: return
    player.start()
  }

  override fun pause() {
    _mediaPlayer?.pause()
  }

  override fun seekToByPercentage(playbackPercentage: Float) {
    val mediaPlayer = _mediaPlayer ?: return
    mediaPlayer.seekTo((mediaPlayer.duration * playbackPercentage).toInt())
  }

  private fun initializeAudioVisualizer(sessionId: Int): AudioVisualizer? {
    return try {
      AudioVisualizer(sessionId)
    } catch (e: Exception) {
      Timber.tag("AudioVisualizer").e(e)
      null
    }
  }
}
