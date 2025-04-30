package io.github.datt16.audioplayer.core.player.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

data class DownloadStatus(
  val contentId: String,
  val progress: Int,
  val state: WorkInfo.State,
)

@OptIn(UnstableApi::class)
class DownloadController @Inject constructor(
  @ApplicationContext private val context: Context,
  private val downloadManager: DownloadManager,
) : DownloadManager.Listener {

  fun startDownload(contentId: String, contentUrl: String) {
    val workTag = "$WORK_PREFIX$contentId"

    val work = OneTimeWorkRequestBuilder<DownloadWorker>()
      .setInputData(
        workDataOf(
          DownloadWorker.KEY_CONTENT_ID to contentId,
          DownloadWorker.KEY_MEDIA_URL to contentUrl
        )
      )
      .addTag(workTag)
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi時のみ
          .build()
      )
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      "media_download_$contentId",
      androidx.work.ExistingWorkPolicy.REPLACE,
      work
    )
  }

  fun pauseDownload(contentId: String) {
    val workTag = "$WORK_PREFIX$contentId"
    WorkManager.getInstance(context).cancelUniqueWork(workTag)
  }

  fun cancelDownload(contentId: String) {
    val workTag = "$WORK_PREFIX$contentId"
    pauseDownload(workTag)
    downloadManager.removeDownload(contentId)
  }

  @kotlin.OptIn(ExperimentalCoroutinesApi::class)
  fun observeProgress(contentId: String): Flow<DownloadStatus> {
    val workTag = "$WORK_PREFIX$contentId"
    return WorkManager.getInstance(context)
      .getWorkInfosForUniqueWorkFlow(workTag)
      .mapLatest { infos ->
        val info = infos.firstOrNull()
        val progress = info?.progress?.getInt(DownloadWorker.KEY_PROGRESS, 0) ?: 0
        DownloadStatus(
          contentId = contentId,
          progress = progress,
          state = info?.state ?: WorkInfo.State.ENQUEUED
        )
      }
      .distinctUntilChanged()
  }

  companion object {
    const val WORK_PREFIX = "media_download_"
  }
}
