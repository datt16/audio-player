package io.github.datt16.audioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.SimpleCache
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.download.DownloadStatus
import io.github.datt16.audioplayer.core.player.util.checkMediaDownloaded
import io.github.datt16.audioplayer.core.player.util.getDownloadedFile
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine

@UnstableApi
class CbcEncryptedDataSource(
  private val contentUri: Uri,
  private val contentId: String,
  private val downloadController: DownloadController,
  private val mediaRepository: MediaRepository,
  private val encryptedFileCache: SimpleCache,
) : BaseDataSource(false) {
  private var bytesRemaining: Long = 0
  private var offset: Long = 0
  private var decryptedData: ByteArray? = null

  override fun open(dataSpec: DataSpec): Long {
    // TODO: エラーハンドリング
    // - dataSpecがnullの場合
    // - dataSpecのpositionが負の値の場合
    // - dataSpecのpositionがmediaFileByteArrayのサイズを超える場合

    transferInitializing(dataSpec)

    // bytesRemainingはC.LENGTH_UNSETで返しても良いらしい
    // transferStarted() を呼ぶまでは再生が始まらないっぽいので、runBlockingでスレッドを止めなくても良いかもしれない
    try {
      // データを並列で読み込む
      val (encryptedData, iv, key) =
        runBlocking {
          val downloadEncryptedFileTask = async { downloadEncryptedMediaFile() }
          val fetchIvTask = async {
            mediaRepository
              .getMediaFiles()
              .map { infos -> infos.firstOrNull { it.mediaId == contentId }?.iv }
              .getOrThrow()
          }
          val fetchKeyTask = async { mediaRepository.getMediaLicense(contentId).getOrThrow() }

          Triple(downloadEncryptedFileTask.await(), fetchIvTask.await(), fetchKeyTask.await())
        }

      val ivByte = iv?.decodeHex() ?: throw Exception("iv is null")
      val keyByte = key.decodeHex()

      decryptedData = decryptDataEncryptedByAesCBC(encryptedData, keyByte, ivByte)
    } catch (e: Exception) {
      throw Exception(
        "再生ファイルの用意に失敗しました code=${PlaybackException.ERROR_CODE_IO_UNSPECIFIED}",
        e
      )
    }

    // 読み取り開始位置を設定
    offset = dataSpec.position.coerceAtMost(decryptedData!!.size.toLong())
    // 残りの読み取り可能なバイト数を計算
    bytesRemaining = decryptedData!!.size - offset
    if (bytesRemaining < 0) {
      throw Exception(
        "再生可能な領域がありません code=${PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE}"
      )
    }

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
    // - decryptedDataがnullの場合

    if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

    val bytesToCopy = length.coerceAtMost(bytesRemaining.toInt())
    System.arraycopy(decryptedData!!, this.offset.toInt(), buffer, offset, bytesToCopy)
    this.offset += bytesToCopy
    bytesRemaining -= bytesToCopy

    return bytesToCopy
  }

  override fun getUri(): Uri {
    return contentUri
  }

  override fun close() {
    bytesRemaining = 0
    offset = 0
    secureCleanup()
  }

  private fun secureCleanup() {
    // decryptedData = null でも十分かもしれないけど、後で要レビュー
    decryptedData?.let { data ->
      data.fill(0)
      decryptedData = null
      System.gc()
    }
  }

  private suspend fun downloadEncryptedMediaFile(): ByteArray {
    return suspendCancellableCoroutine { continuation ->
      try {
        val scope = CoroutineScope(continuation.context)
        // キャッシュにデータが残ってたらそれ使う
        if (encryptedFileCache.checkMediaDownloaded(mediaId = contentId)) {
          val data = encryptedFileCache.getDownloadedFile(mediaId = contentId).readBytes()
          continuation.resumeWith(Result.success(data))
          return@suspendCancellableCoroutine
        }

        // キャッシュにデータ残ってなかったら新しくデータをダウンロードする
        val job =
          scope.launch {
            downloadController.startDownload(contentId, contentUri).collect { status ->
              when (status) {
                is DownloadStatus.Downloading, is DownloadStatus.Enqueued -> Unit
                is DownloadStatus.Failed -> {
                  continuation.resumeWithException(Exception("Download failed"))
                }

                is DownloadStatus.Success -> {
                  val data =
                    encryptedFileCache
                      .getDownloadedFile(mediaId = contentId)
                      .readBytes()
                  continuation.resumeWith(Result.success(data))
                }
              }
            }
          }

        continuation.invokeOnCancellation { job.cancel() }
      } catch (e: Exception) {
        continuation.resumeWithException(e)
      }
    }
  }

  private fun decryptDataEncryptedByAesCBC(
    encryptedData: ByteArray,
    keyData: ByteArray,
    ivData: ByteArray
  ): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/NoPadding")
    val keySpec = SecretKeySpec(keyData, "AES")
    val ivSpec = IvParameterSpec(ivData)
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

    return encryptedData.let { encrypted -> cipher.doFinal(encrypted) }
  }

  private fun String.decodeHex(): ByteArray {
    return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
  }
}
