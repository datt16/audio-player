package io.github.datt16.audioplayer.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.datt16.audioplayer.core.data.model.MediaFile
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import java.text.DecimalFormat

@Composable
fun MediaFileList(
  mediaFiles: List<MediaFileItemState>,
  onClickMediaItem: (mediaFile: MediaFile) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(mediaFiles) { mediaFileState ->
      when (mediaFileState) {
        is MediaFileItemState.Loaded -> {
          MediaFileCard(
            mediaFile = mediaFileState.mediaFile,
            onClick = { onClickMediaItem(mediaFileState.mediaFile) },
            modifier = Modifier.fillMaxWidth(),
            hasBorder = true
          )
        }

        is MediaFileItemState.Loading -> {
          Box(modifier = Modifier.fillMaxWidth()) {
            MediaFileCard(
              mediaFile = mediaFileState.mediaFile,
              onClick = {},
              modifier = Modifier.fillMaxWidth()
            )
            CircularProgressIndicator(
              modifier = Modifier.align(Alignment.Center)
            )
          }
        }

        is MediaFileItemState.NotLoaded -> {
          MediaFileCard(
            mediaFile = mediaFileState.mediaFile,
            onClick = { onClickMediaItem(mediaFileState.mediaFile) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
fun MediaFileCard(
  mediaFile: MediaFile,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  hasBorder: Boolean = false,
) {
  Card(
    modifier = modifier.clickable { onClick() },
    border = if (hasBorder) {
      BorderStroke(
        4.dp,
        Brush.sweepGradient(
          colors = listOf(
            AudioPlayerAppTheme.colors.primary,
            AudioPlayerAppTheme.colors.secondary
          )
        )
      )
    } else {
      null
    }
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
        imageVector = if (mediaFile.isEncrypted) Icons.Default.Lock else Icons.Default.CheckCircle,
        contentDescription = if (mediaFile.isEncrypted) "Encrypted" else "Not encrypted",
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
