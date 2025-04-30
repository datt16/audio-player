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
      delay(1000)
      val download = downloadManager.downloadIndex.getDownload(request.id)
      state = download?.state ?: Download.STATE_FAILED

      val progressPercentage = (download?.percentDownloaded ?: 0.0).toInt()
      setProgress(workDataOf(KEY_PROGRESS to progressPercentage))
    } while (state == Download.STATE_DOWNLOADING || state == Download.STATE_QUEUED)

    downloadManager.release()

    if (state == Download.STATE_COMPLETED) Result.success() else Result.failure()
  }

  companion object {
    const val KEY_CONTENT_ID = "CONTENT_ID"
    const val KEY_MEDIA_URL = "MEDIA_URI"
    const val KEY_PROGRESS = "PROGRESS"
  }
}
