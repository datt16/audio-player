package io.github.datt16.audioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec

/**
 * 単純にByteArrayをDataSourceとして扱う実装
 * メモ用
 */
@UnstableApi
class BasicOnMemoryDataSource(private val mediaFileByteArray: ByteArray) : BaseDataSource(false) {
  private var bytesRemaining: Long = 0
  private var offset: Long = 0

  override fun open(dataSpec: DataSpec): Long {
    // TODO: エラーハンドリング
    // - dataSpecがnullの場合
    // - dataSpecのpositionが負の値の場合
    // - dataSpecのpositionがmediaFileByteArrayのサイズを超える場合
    // - dataSpecのlengthが負の値の場合

    transferInitializing(dataSpec)

    // 読み取り開始位置を設定
    offset = dataSpec.position.coerceAtMost(mediaFileByteArray.size.toLong())
    // 残りの読み取り可能なバイト数を計算
    bytesRemaining = mediaFileByteArray.size - offset

    transferStarted(dataSpec)

    return bytesRemaining
  }

  override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
    // TODO: エラーハンドリング
    // - bufferがnullの場合
    // - offsetが負の値の場合
    // - lengthが負の値の場合
    // - offset + lengthがbufferのサイズを超える場合
    // - メモリ不足の場合

    if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

    val bytesToCopy = length.coerceAtMost(bytesRemaining.toInt())
    System.arraycopy(mediaFileByteArray, this.offset.toInt(), buffer, offset, bytesToCopy)
    this.offset += bytesToCopy
    bytesRemaining -= bytesToCopy

    return bytesToCopy
  }

  override fun getUri(): Uri? {
    return null
  }

  override fun close() {
    bytesRemaining = 0
    offset = 0
  }
}
