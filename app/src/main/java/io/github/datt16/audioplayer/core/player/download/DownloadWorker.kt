package io.github.datt16.audioplayer.core.player.download

import android.content.Context

class DownloadWorker(
  private val context: Context
)  {
  companion object {
    const val KEY_CONTENT_ID = "CONTENT_ID"
    const val KEY_MEDIA_URI = "MEDIA_URI"
  }
}
