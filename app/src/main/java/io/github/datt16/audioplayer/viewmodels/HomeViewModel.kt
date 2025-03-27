package io.github.datt16.audioplayer.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.PlaybackManager
import io.github.datt16.audioplayer.screens.home.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val playbackManager: PlaybackManager
) : ViewModel() {

  fun getExoPlayer() = playbackManager.player

  fun startPlayback(url: String) {
    playbackManager.setup(url.toUri())
    playbackManager.play()
  }
}
