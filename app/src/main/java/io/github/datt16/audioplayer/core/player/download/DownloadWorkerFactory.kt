package io.github.datt16.audioplayer.core.player.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

@UnstableApi
class DownloadWorkerFactory(
  private val downloadManager: DownloadManager,
) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    return when (workerClassName) {
      DownloadWorker::class.java.name ->
        DownloadWorker(appContext, workerParameters, downloadManager)

      else -> null
    }
  }
}
