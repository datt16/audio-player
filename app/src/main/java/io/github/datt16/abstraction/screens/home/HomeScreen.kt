package io.github.datt16.abstraction.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.datt16.abstraction.core.designsystem.AbstractionAppTheme
import io.github.datt16.abstraction.core.ext.black
import io.github.datt16.abstraction.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = hiltViewModel(),
  sampleKey: String = "datt16",
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    if (uiState.isLoading) {
      Box(
        Modifier.fillMaxWidth().padding(16.dp)
      ) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center)
        )
      }
    } else {
      Text(
        style = AbstractionAppTheme.typography.displayMedium.black(),
        text = "Hello $sampleKey,\nThis is Home Screen",
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Preview
@Composable
private fun HomeScreenPreview() {
  AbstractionAppTheme {
    HomeScreen()
  }
}
