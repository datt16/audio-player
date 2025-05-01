package io.github.datt16.audioplayer.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.viewmodels.CacheItem
import io.github.datt16.audioplayer.viewmodels.GlobalSettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: GlobalSettingsViewModel = hiltViewModel(),
) {
  val scope = rememberCoroutineScope()
  var isRefreshing by remember { mutableStateOf(false) }
  val progress by viewModel.downloadProgress.collectAsState()
  val cacheEntries by viewModel.cacheItems.collectAsState()

  LaunchedEffect(Unit) {
    viewModel.checkCacheEntries()
  }

  PullToRefreshBox(
    modifier = modifier.fillMaxSize(),
    isRefreshing = isRefreshing,
    onRefresh = {
      scope.launch {
        isRefreshing = true
        viewModel.checkCacheEntries()
        delay(500)
        isRefreshing = false
      }
    }
  ) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
      item {
        Text(
          text = "Download Progress",
          style = AudioPlayerAppTheme.typography.titleMedium,
          color = AudioPlayerAppTheme.colors.onSurface,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
      progress?.entries?.forEach { (key, value) ->
        item(key) {
          Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(key)
            Spacer(modifier = Modifier.weight(1f))
            Column {
              Text("${value.progress}%")
              Text(value.state.name)
            }
          }
        }
      }
      item {
        ElevatedButton(
          onClick = { viewModel.startDownloadSample() },
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
        ) {
          Text("Download Sample")
        }
      }
      item {
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }
      item {
        Text(
          text = "Cached Files (${cacheEntries.size})",
          style = AudioPlayerAppTheme.typography.titleMedium,
          color = AudioPlayerAppTheme.colors.onSurface,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
      items(cacheEntries) {
        CacheEntryItem(
          it,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun CacheEntryItem(cacheItem: CacheItem, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .height(80.dp)
      .background(
        color = AudioPlayerAppTheme.colors.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(16.dp)
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
        text = "key: ${cacheItem.key}",
        style = AudioPlayerAppTheme.typography.labelMedium,
        color = AudioPlayerAppTheme.colors.onSurface,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "${cacheItem.sizeMb}MB",
        style = AudioPlayerAppTheme.typography.labelMedium,
        color = AudioPlayerAppTheme.colors.onSurface,
      )
    }
    Text(
      text = cacheItem.path,
      style = AudioPlayerAppTheme.typography.labelMedium,
      color = AudioPlayerAppTheme.colors.surfaceVariant,
    )
  }
}
