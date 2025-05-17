package io.github.datt16.audioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

@UnstableApi
class OnMemoryDataSourceProvider(
  private val contentUri: Uri,
  private val contentId: String,
) : DataSource.Factory {
  private val scope = CoroutineScope(Dispatchers.IO)
  private var dataSource: DataSource? = null

  override fun createDataSource(): DataSource {
    TODO("Not yet implemented")
  }

  private suspend fun downloadMediaFile(contentUri: Uri, contentId: String) {

  }

  private fun getMediaFile(): File {
    TODO()
  }

  private suspend fun getEncryptionKey(contentId: String): ByteArray {
    TODO()
  }

  private fun decryptMediaFile(encryptedFile: File, key: ByteArray): ByteArray {
    TODO()
  }

  private fun createOnMemoryDataSource(decryptedData: ByteArray): DataSource {
    return BasicOnMemoryDataSource(decryptedData)
  }

  private fun release() {

  }
}