package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.AudioLevelManager
import io.github.datt16.audioplayer.core.player.ExoPlayerPlaybackManager
import io.github.datt16.audioplayer.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel
@Inject
constructor(
  private val playbackManager: ExoPlayerPlaybackManager,
  audioLevelManager: AudioLevelManager,
  private val mediaRepository: MediaRepository,
) : ViewModel() {

  val duration
    get() = playbackManager.duration
  val playbackFlow = playbackManager.playbackProgressFlow

  val audioLevelFlow = audioLevelManager.audioLevelFlow

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentPlayingUrl = MutableStateFlow<String?>(null)

  fun startPlayback(mediaId: String) {
    viewModelScope.launch {
      try {
        val url = "http://10.0.2.2:8888/api/media/$mediaId"
        playbackManager.setup(url.toUri())
        playbackManager.play()
        _currentPlayingUrl.value = url
        _isPlaying.value = true
      } catch (e: Exception) {
        // エラーハンドリング
        _isPlaying.value = false
        _currentPlayingUrl.value = null
      }
    }
  }

  fun play() {
    _isPlaying.value = true
    playbackManager.play()
  }

  fun pause() {
    _isPlaying.value = false
    playbackManager.pause()
  }

  fun seekTo(percentage: Float) {
    playbackManager.seekToByPercentage(percentage)
  }

  fun fetchMediaFiles() {
    viewModelScope.launch {
      _uiState.value = HomeUiState.Loading
      mediaRepository
        .getMediaFiles()
        .onSuccess { files -> _uiState.value = HomeUiState.Success(files) }
        .onFailure { error ->
          _uiState.value =
            HomeUiState.Error(message = error.message ?: "不明なエラーが発生しました")
        }
    }
  }
}
