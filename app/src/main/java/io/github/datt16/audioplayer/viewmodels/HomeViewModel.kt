package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.PlaybackManager
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel
@Inject constructor(
  private val playbackManager: PlaybackManager,
) : ViewModel() {

  fun getExoPlayer() = playbackManager.exoPlayer

  val playbackFlow = playbackManager.getPlaybackProgressFlow()
  val audioFrequencyMapFlow =
    playbackManager.audioVisualizer?.frequencyMapFlow ?: emptyFlow()

  fun startPlayback(url: String) {
    playbackManager.setup(url.toUri())
    playbackManager.play()
  }

  fun play() {
    playbackManager.play()
  }

  fun pause() {
    playbackManager.pause()
  }

  fun seekTo(percentage: Float) {
    playbackManager.seekToByPercentage(percentage)
  }
}
