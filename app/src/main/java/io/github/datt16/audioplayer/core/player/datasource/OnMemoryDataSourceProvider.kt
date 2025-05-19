package io.github.datt16.audioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.SimpleCache
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.download.DownloadController

data class DataSourceDebugFlags(
  val skipDecryptStep: Boolean = false,
) {
  companion object {
    val DEFAULT = DataSourceDebugFlags()
  }
}

@UnstableApi
class OnMemoryDataSourceProvider(
  private val contentUri: Uri,
  private val contentId: String,
  private val downloadController: DownloadController,
  private val mediaRepository: MediaRepository,
  private val encryptedFileCache: SimpleCache,
  private val debugFlags: DataSourceDebugFlags = DataSourceDebugFlags.DEFAULT,
) : DataSource.Factory {
  override fun createDataSource(): DataSource {
    return CbcEncryptedDataSource(
      contentUri = contentUri,
      contentId = contentId,
      downloadController = downloadController,
      mediaRepository = mediaRepository,
      encryptedFileCache = encryptedFileCache,
      debugFlags = debugFlags,
    )
  }
}
