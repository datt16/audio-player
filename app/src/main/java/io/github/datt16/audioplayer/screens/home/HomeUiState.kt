package io.github.datt16.audioplayer.screens.home

import io.github.datt16.audioplayer.core.data.model.MediaFile

sealed interface HomeUiState {
  data object Loading : HomeUiState

  data class Success(val mediaFiles: List<MediaFile>) : HomeUiState

  data class Error(val message: String) : HomeUiState
}
