package io.github.datt16.audioplayer.core.player

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener

/**
 * TODO: media3のByteArrayDataSourceと比較する
 */
@UnstableApi
class ByteArrayDataSource(
  private val decryptedData: ByteArray,
  private val listener: Listener? = null,
) : DataSource {
  interface Listener {
    fun onClose() // for clear memory
  }

  private var bytesRemaining: Long = 0
  private var offset: Long = 0

  override fun open(dataSpec: DataSpec): Long {
    val skip = dataSpec.position
    offset = skip.coerceAtMost(decryptedData.size.toLong())
    bytesRemaining = decryptedData.size - offset
    return bytesRemaining
  }

  override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
    if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

    val bytesToCopy = length.coerceAtMost(bytesRemaining.toInt())
    System.arraycopy(decryptedData, this.offset.toInt(), buffer, offset, bytesToCopy)
    this.offset += bytesToCopy
    bytesRemaining -= bytesToCopy

    return bytesToCopy
  }

  override fun close() {
    listener?.onClose()
  }

  override fun addTransferListener(transferListener: TransferListener) {
    // Noop
  }

  override fun getUri(): Uri? = Uri.EMPTY
}
