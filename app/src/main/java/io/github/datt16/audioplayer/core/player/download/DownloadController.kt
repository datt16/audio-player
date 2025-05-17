package io.github.datt16.audioplayer.core.player.download

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.datt16.audioplayer.BuildConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import javax.inject.Inject

sealed interface DownloadStatus {
  val contentId: String

  data class Enqueued(
    override val contentId: String,
  ) : DownloadStatus

  data class Downloading(
    override val contentId: String,
    val progress: Int
  ) : DownloadStatus

  data class Failed(
    override val contentId: String,
    val state: WorkInfo.State,
  ) : DownloadStatus

  data class Success(
    override val contentId: String,
  ) : DownloadStatus
}

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalCoroutinesApi::class)
class DownloadController @Inject constructor(
  @ApplicationContext private val context: Context,
  private val downloadManager: DownloadManager,
) : DownloadManager.Listener {

  fun startDownload(contentId: String, contentUrl: String): Flow<DownloadStatus> {
    return startDownload(contentId, contentUrl.toUri())
  }

  fun startDownload(contentId: String, contentUri: Uri): Flow<DownloadStatus> {
    val uniqueName = generateUniqueId(contentId, PREFIX_DOWNLOAD_WORK)
    val contentUniqueTag = generateUniqueId(contentId, PREFIX_DOWNLOAD_CONTENT)
    val contentIdTag = "$PREFIX_DOWNLOAD_CONTENT$contentId"

    val work = OneTimeWorkRequestBuilder<DownloadWorker>()
      .setInputData(
        workDataOf(
          DownloadWorker.KEY_CONTENT_ID to contentId,
          DownloadWorker.KEY_MEDIA_URL to contentUri
        )
      )
      .addTag(GROUP_DOWNLOAD_ALL)
      .addTag(contentUniqueTag)
      .addTag(contentIdTag)
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .build()
      )
//      .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // 可能な限り早く実行・ForegroundService実行の割当量が制限されている場合は、通常の優先度に戻す
      .build()

    /**
     * setExpedited() を利用すればさらに実行優先度を上げられる
     * POST_NOTIFICATIONSのマニフェスト宣言が必要、And12以下ではforegroundServiceの宣言が必要かも
     */

    WorkManager.getInstance(context).enqueueUniqueWork(
      uniqueName,
      androidx.work.ExistingWorkPolicy.REPLACE,
      work
    )

    return WorkManager.getInstance(context).getWorkInfosByTagFlow(contentIdTag)
      .mapLatest { infos -> workStateToDownloadStatus(contentId, infos.firstOrNull()) }
  }

  fun pauseDownload(contentId: String) {

    // TODO: keyの中からcontentIdを含んでるやつを探してそのuniqueNameをとってくる
    val uniqueName = generateUniqueId(contentId, PREFIX_DOWNLOAD_WORK)
    WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
  }

  fun cancelDownload(contentId: String) {
    pauseDownload(contentId)
    downloadManager.removeDownload(contentId)
  }

  @kotlin.OptIn(ExperimentalCoroutinesApi::class)
  fun getDownloadProgressFlow(contentId: String): Flow<DownloadStatus> {
    val contentIdTag = "$PREFIX_DOWNLOAD_CONTENT$contentId"
    return WorkManager.getInstance(context)
      .getWorkInfosByTagFlow(contentIdTag)
      .mapLatest { infos ->
        val info = infos.firstOrNull()
        workStateToDownloadStatus(contentId, info)
      }
      .distinctUntilChanged()
  }

  @kotlin.OptIn(ExperimentalCoroutinesApi::class)
  fun getAllDownloadProgressMapFlow(): Flow<Map<String, DownloadStatus>> {
    return WorkManager.getInstance(context)
      .getWorkInfosByTagFlow(GROUP_DOWNLOAD_ALL)
      .mapLatest { infos ->
        infos
          .mapNotNull { info ->
            // タグから contentId を抽出
            val contentTag = info.tags.firstOrNull { it.startsWith(PREFIX_DOWNLOAD_CONTENT) }
            val contentId =
              contentTag?.removePrefix(PREFIX_DOWNLOAD_CONTENT) ?: return@mapNotNull null
            "work-$contentId" to workStateToDownloadStatus(contentId, info)
          }
          .toMap()
      }
      // Map の中身が変わらない限り流さない
      .distinctUntilChanged()
  }

  private fun workStateToDownloadStatus(contentId: String, info: WorkInfo?): DownloadStatus {
    return when (info?.state) {
      null, WorkInfo.State.ENQUEUED -> DownloadStatus.Enqueued(contentId)
      WorkInfo.State.SUCCEEDED -> {
        DownloadStatus.Success(contentId)
      }

      WorkInfo.State.FAILED, WorkInfo.State.BLOCKED, WorkInfo.State.CANCELLED -> {
        DownloadStatus.Failed(contentId, info.state)
      }

      WorkInfo.State.RUNNING -> {
        val startTime = info.outputData.getLong("start_time", 0)
        val currentTime = System.currentTimeMillis()
        Timber.tag("osa").d(
          """
            workId: ${info.id}
            workTag: ${info.tags}
            execute from: ${(currentTime - startTime) / 60 / 1000}min
          """.trimIndent()
        )
        val progress = info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
        DownloadStatus.Downloading(contentId, progress)
      }
    }
  }

  private fun generateUniqueId(contentId: String, prefix: String): String {
    val timestamp = System.currentTimeMillis()
    val appVersionName = BuildConfig.VERSION_NAME

    return "$prefix${appVersionName}_${contentId}_$timestamp"
  }

  companion object {
    private const val PREFIX_DOWNLOAD_WORK = "media_download_"
    private const val PREFIX_DOWNLOAD_CONTENT = "media_download_content_"
    private const val GROUP_DOWNLOAD_ALL = "media_download_all"
  }
}
