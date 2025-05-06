package io.github.datt16.audioplayer.screens.home

import io.github.datt16.audioplayer.core.data.model.MediaFile
import io.github.datt16.audioplayer.core.player.download.DownloadStatus

sealed interface HomeUiState {
  data object Loading : HomeUiState

  data class Success(
    val mediaFiles: List<MediaFileItemState>
  ) : HomeUiState

  data class Error(val message: String, val sampleMediaList: List<MediaFileItemState>) : HomeUiState
}

sealed interface MediaFileItemState {
  val mediaFile: MediaFile

  data class NotLoaded(
    override val mediaFile: MediaFile
  ) : MediaFileItemState

  data class Loading(
    override val mediaFile: MediaFile,
    val status: DownloadStatus
  ) : MediaFileItemState

  data class Loaded(
    override val mediaFile: MediaFile,
    val isFailed: Boolean,
  ) : MediaFileItemState
}
