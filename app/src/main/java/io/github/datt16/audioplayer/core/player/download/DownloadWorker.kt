package io.github.datt16.audioplayer.core.player.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import timber.log.Timber

@UnstableApi
class DownloadWorker(
  appContext: Context,
  params: WorkerParameters,
  private val downloadManager: DownloadManager,
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result = coroutineScope {
    val mediaUrl = inputData.getString(KEY_MEDIA_URL) ?: return@coroutineScope Result.failure()
    val contentId = inputData.getString(KEY_CONTENT_ID) ?: return@coroutineScope Result.failure()
    val request = DownloadRequest.Builder(contentId, mediaUrl.toUri()).build()
    downloadManager.addDownload(request)
    downloadManager.resumeDownloads()

    var state: Int
    do {
      delay(500)
      val activeDownload = downloadManager.currentDownloads
        .firstOrNull { it.request.id == request.id }

      val indexedDownload = downloadManager.downloadIndex.getDownload(request.id)
      state = activeDownload?.state
        ?: indexedDownload?.state
          ?: Download.STATE_FAILED

      val progress = when (state) {
        Download.STATE_COMPLETED -> 100                       // 完了なら 100%
        Download.STATE_FAILED -> 0                         // 失敗なら 0%
        else -> (activeDownload?.percentDownloaded?.toInt() ?: 0)
      }
      setProgress(workDataOf(KEY_PROGRESS to progress))
    } while (state == Download.STATE_QUEUED || state == Download.STATE_DOWNLOADING)

    downloadManager.release()

    if (state == Download.STATE_COMPLETED) Result.success() else Result.failure()
  }

  companion object {
    const val KEY_CONTENT_ID = "CONTENT_ID"
    const val KEY_MEDIA_URL = "MEDIA_URI"
    const val KEY_PROGRESS = "PROGRESS"
  }
}
