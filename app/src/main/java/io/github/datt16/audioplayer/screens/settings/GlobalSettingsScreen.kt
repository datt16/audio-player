package io.github.datt16.audioplayer.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.datt16.audioplayer.viewmodels.GlobalSettingsViewModel

@Composable
fun GlobalSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: GlobalSettingsViewModel = hiltViewModel()
) {
  val progress = viewModel.downloadProgress.collectAsState()

  LazyColumn(modifier = modifier.fillMaxSize()) {
    item {
      Text("設定画面")
    }
    item {
      Text(progress.toString())
    }
    item {
      ElevatedButton(
        onClick = { viewModel.startDownloadSample() },
        modifier = Modifier.fillMaxSize()
      ) {
        Text("Download Sample")
      }
    }
  }
}
