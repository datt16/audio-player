package io.github.datt16.audioplayer.core.player.download

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import javax.inject.Inject

@OptIn(UnstableApi::class)
class DownloadController @Inject constructor(
  private val downloadManager: DownloadManager,
): DownloadManager.Listener {


  fun requestDownload(contentId: String, contentUri: Uri) {
    // TODO: request worker
    // TODO: save workerId and contentId map
  }
}
