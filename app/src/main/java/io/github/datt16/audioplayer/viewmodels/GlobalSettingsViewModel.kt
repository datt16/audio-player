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
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
  private val downloadController: DownloadController
) : ViewModel() {
  val downloadProgress = downloadController.getAllDownloadProgressMapFlow()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = null,
    )

  fun startDownloadSample() {
    startDownload("sample01", "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
  }

  private fun startDownload(contentId: String, contentUrl: String) {
    downloadController.startDownload(contentId, contentUrl)
  }

  fun pauseDownload(contentId: String) {
    downloadController.pauseDownload(contentId)
  }

  fun cancelDownload(contentId: String) {
    downloadController.cancelDownload(contentId)
  }
}
