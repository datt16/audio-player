package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.ExoPlayerPlaybackManager
import io.github.datt16.audioplayer.core.player.PlaybackManager
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val playbackManager: ExoPlayerPlaybackManager,
) : ViewModel() {

  val isPlaying
    get() = playbackManager.isPlaying
  val duration
    get() = playbackManager.duration
  val playbackFlow = playbackManager.playbackProgressFlow

  val audioFrequencyMapFlow
    get() = playbackManager.audioVisualizer?.frequencyMapFlow ?: emptyFlow()

  fun startPlayback(url: String) {
    viewModelScope.launch {
      playbackManager.setup(url.toUri())
      playbackManager.play()
    }
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
