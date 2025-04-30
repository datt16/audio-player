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
import kotlinx.coroutines.flow.Flow
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
    val uniqueName = UNIQUE_WORK_PREFIX + contentId
    val contentTag = CONTENT_TAG_PREFIX + contentId

    val work = OneTimeWorkRequestBuilder<DownloadWorker>()
      .setInputData(
        workDataOf(
          DownloadWorker.KEY_CONTENT_ID to contentId,
          DownloadWorker.KEY_MEDIA_URL to contentUrl
        )
      )
      .addTag(GROUP_TAG)
      .addTag(contentTag)
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi時のみ
          .build()
      )
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      uniqueName,
      androidx.work.ExistingWorkPolicy.REPLACE,
      work
    )
  }

  fun pauseDownload(contentId: String) {
    val uniqueName = UNIQUE_WORK_PREFIX + contentId
    WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
  }

  fun cancelDownload(contentId: String) {
    pauseDownload(contentId)
    downloadManager.removeDownload(contentId)
  }

  @kotlin.OptIn(ExperimentalCoroutinesApi::class)
  fun getDownloadProgressFlow(contentId: String): Flow<DownloadStatus> {
    return WorkManager.getInstance(context)
      .getWorkInfosForUniqueWorkFlow(GROUP_TAG)
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

  @kotlin.OptIn(ExperimentalCoroutinesApi::class)
  fun getAllDownloadProgressMapFlow(): Flow<Map<String, DownloadStatus>> {
    return WorkManager.getInstance(context)
      .getWorkInfosByTagFlow(GROUP_TAG)
      .mapLatest { infos ->
        infos
          .mapNotNull { info ->
            // タグから contentId を抽出
            val contentTag = info.tags.firstOrNull { it.startsWith(CONTENT_TAG_PREFIX) }
            val contentId =
              contentTag?.removePrefix(CONTENT_TAG_PREFIX) ?: return@mapNotNull null
            val percent = info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
            "work-$contentId" to DownloadStatus(
              contentId = contentId,
              progress = if (info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED) percent else 100,
              state = info.state
            )
          }
          .toMap()
      }
      // Map の中身が変わらない限り流さない
      .distinctUntilChanged()
  }

  companion object {
    private const val UNIQUE_WORK_PREFIX = "media_download_"
    private const val CONTENT_TAG_PREFIX = "media_download_content_"
    private const val GROUP_TAG = "media_download_all"
  }
}
