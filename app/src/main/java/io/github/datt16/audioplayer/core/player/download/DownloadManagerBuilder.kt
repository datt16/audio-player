package io.github.datt16.audioplayer.core.player.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executor

@UnstableApi
interface DownloadManagerBuilder {
  companion object {
    fun buildDownloadManager(
      context: Context,
      cache: SimpleCache,
      databaseProvider: StandaloneDatabaseProvider,
      dataSourceFactory: DataSource.Factory,
    ): DownloadManager {
      val downloadExecutor = Executor(Runnable::run)

      return DownloadManager(
        context,
        databaseProvider,
        cache,
        dataSourceFactory,
        downloadExecutor
      )
    }

    fun buildCache(context: Context, databaseProvider: StandaloneDatabaseProvider): SimpleCache {
      val downloadDir = File(context.cacheDir, "caches")
      return SimpleCache(downloadDir, NoOpCacheEvictor(), databaseProvider)
    }
  }
}
