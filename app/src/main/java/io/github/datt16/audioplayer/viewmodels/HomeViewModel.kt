package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.ExoPlayerPlaybackManager
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val exoPlayerPlaybackManager: ExoPlayerPlaybackManager,
) : ViewModel() {

  val isPlaying
    get() = exoPlayerPlaybackManager.isPlaying
  val duration
    get() = exoPlayerPlaybackManager.duration
  val playbackFlow = exoPlayerPlaybackManager.playbackProgressFlow

  val audioFrequencyMapFlow =
    exoPlayerPlaybackManager.audioVisualizer?.frequencyMapFlow ?: emptyFlow()

  fun startPlayback(url: String) {
    exoPlayerPlaybackManager.setup(url.toUri())
    exoPlayerPlaybackManager.play()
  }

  fun play() {
    exoPlayerPlaybackManager.play()
  }

  fun pause() {
    exoPlayerPlaybackManager.pause()
  }

  fun seekTo(percentage: Float) {
    exoPlayerPlaybackManager.seekToByPercentage(percentage)
  }
}
