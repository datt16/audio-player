package io.github.datt16.audioplayer.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerAppScaffold(
  modifier: Modifier = Modifier,
  content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text("Audio Player")
        }
      )
    },
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = "Home"
            )
          },
          label = { Text("Home") },
          selected = true,
          onClick = {}
        )
      }
    }
  ) {
    content(it)
  }
}

@Preview
@Composable
private fun AudioPlayerAppScaffoldPreview() {
  AudioPlayerAppTheme {
    AudioPlayerAppScaffold {
      Column {
        Text("Hello, This is sample text")
      }
    }
  }
}
