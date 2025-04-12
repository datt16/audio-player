package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.data.model.MediaFile
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.AudioLevelManager
import io.github.datt16.audioplayer.core.player.ExoPlayerPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val playbackManager: ExoPlayerPlaybackManager,
  audioLevelManager: AudioLevelManager,
  private val mediaRepository: MediaRepository,
) : ViewModel() {

  val isPlaying
    get() = playbackManager.isPlaying
  val duration
    get() = playbackManager.duration
  val playbackFlow = playbackManager.playbackProgressFlow

  val audioFrequencyMapFlow
    get() = playbackManager.audioVisualizer?.frequencyMapFlow ?: emptyFlow()

  val audioLevelFlow = audioLevelManager.audioLevelFlow

  private val _mediaFiles = MutableStateFlow<List<MediaFile>>(emptyList())
  val mediaFiles = _mediaFiles.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

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

  fun fetchMediaFiles() {
    viewModelScope.launch {
      _isLoading.value = true
      mediaRepository.getMediaFiles().onSuccess { files -> _mediaFiles.value = files }.onFailure {
          error ->
        Timber.e(error)
        // エラーハンドリングが必要な場合は、ここでUIに通知するなどの処理を追加
      }
      _isLoading.value = false
    }
  }
}
