package io.github.datt16.audioplayer.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.datt16.audioplayer.core.data.model.MediaFile
import io.github.datt16.audioplayer.viewmodels.HomeViewModel
import java.text.DecimalFormat

@Composable
fun MediaFileList(
  mediaFiles: List<MediaFile>,
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(mediaFiles) { mediaFile ->
      MediaFileCard(
        mediaFile = mediaFile,
        onClick = { viewModel.startPlayback(mediaFile.mediaId) },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
fun MediaFileCard(mediaFile: MediaFile, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.clickable { onClick() },
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = mediaFile.name,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // ファイルタイプ
          Text(
            text = mediaFile.type.uppercase(),
            style = MaterialTheme.typography.labelMedium
          )
          // ファイルサイズ
          Text(
            text = formatFileSize(mediaFile.size),
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
      // 暗号化状態のアイコン
      Icon(
        imageVector =
        if (isEncrypted(mediaFile.path)) {
          Icons.Default.Lock
        } else {
          Icons.Default.CheckCircle
        },
        contentDescription =
        if (isEncrypted(mediaFile.path)) {
          "Encrypted"
        } else {
          "Not encrypted"
        },
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}

@Composable
fun ErrorContent(message: String, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(text = message, style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Button(
      onClick = onRefresh,
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("再読み込み")
    }
  }
}

private fun formatFileSize(size: Long): String {
  val df = DecimalFormat("#.##")
  return when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${df.format(size / 1024.0)} KB"
    size < 1024 * 1024 * 1024 -> "${df.format(size / (1024.0 * 1024.0))} MB"
    else -> "${df.format(size / (1024.0 * 1024.0 * 1024.0))} GB"
  }
}

private fun isEncrypted(path: String): Boolean {
  return path.contains("encrypted", ignoreCase = true)
}
