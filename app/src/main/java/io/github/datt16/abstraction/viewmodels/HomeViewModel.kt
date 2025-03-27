package io.github.datt16.abstraction.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.abstraction.screens.home.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class HomeViewModelState(
  val isLoading: Boolean,
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

  private val vmState = MutableStateFlow(HomeViewModelState(isLoading = false))
  val uiState: StateFlow<HomeUiState> = vmState.map { vmState ->
    HomeUiState(
      isLoading = vmState.isLoading,
      username = "datt16",
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = HomeUiState.Dummy,
  )

  init {
    viewModelScope.launch {
      vmState.update { it.copy(isLoading = true) }
      delay(DELAY_MILLIS)
      vmState.update { it.copy(isLoading = false) }
    }
  }

  companion object {
    const val DELAY_MILLIS = 1000L
  }
}
