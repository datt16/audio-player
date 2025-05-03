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

  fun startPlayback(mediaFile: MediaFile) {
    viewModelScope.launch {
      try {
        val url = if (mediaFile.mediaId.startsWith("sample")) {
          mediaFile.playableUrl
        } else {
          "http://10.0.2.2:8888/api/media/${mediaFile.mediaId}"
        }

        if (mediaFile.isEncrypted) {
          val key = mediaRepository.getMediaLicense(mediaFile.mediaId).getOrNull()
          val iv = mediaFile.iv
          if (key == null || iv == null) {
            // TODO: エラー表示
            return@launch
          }

          playbackManager.setup(uri = url.toUri(), iv = iv, key = key)
        } else {
          playbackManager.setup(url.toUri())
        }


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

  companion object {
    val sampleMediaFiles = listOf(
      MediaFile(
        name = "sample01(オフライン再生用)",
        path = "",
        size = 0,
        type = "audio",
        isDir = false,
        relativePath = "",
        mediaId = "sample01",
        playableUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        iv = null,
        isEncrypted = false,
      )
    )
  }
}
