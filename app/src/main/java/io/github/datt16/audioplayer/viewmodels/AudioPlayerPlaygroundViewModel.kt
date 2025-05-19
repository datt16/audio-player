package io.github.datt16.audioplayer.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.AudioPlayerManager
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class AudioPlayerPlaygroundViewModel @Inject constructor(
  private val audioPlayerManager: AudioPlayerManager,
) : ViewModel() {

  fun initialize() {
    audioPlayerManager.initialize()
  }

  fun prepare(contentId: String, contentUrl: String) {
    audioPlayerManager.prepare(contentUrl.toUri(), contentId)
//    audioPlayerManager.play()
  }

  fun play() {
    audioPlayerManager.play()
  }

  fun pause() {
    audioPlayerManager.stop()
  }
}
