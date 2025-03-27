package io.github.datt16.audioplayer.screens.home

data class HomeUiState(
  val isLoading: Boolean,
  val username: String,
) {
  companion object {
    val Dummy = HomeUiState(isLoading = false, username = "datt16")
  }
}
