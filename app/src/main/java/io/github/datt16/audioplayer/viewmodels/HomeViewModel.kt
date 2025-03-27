package io.github.datt16.audioplayer.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.PlaybackManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val playbackManager: PlaybackManager,
) : ViewModel() {

  fun getExoPlayer() = playbackManager.player

  fun startPlayback(url: String) {
    playbackManager.setup(url.toUri())
    playbackManager.play()
  }
}
