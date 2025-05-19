package io.github.datt16.audioplayer.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.download.DownloadManagerBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {
  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  fun provideDatabaseProvider(
    @ApplicationContext context: Context,
  ): StandaloneDatabaseProvider {
    return StandaloneDatabaseProvider(context)
  }

  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  fun provideEncryptedFileCache(
    @ApplicationContext context: Context,
    databaseProvider: StandaloneDatabaseProvider,
  ): SimpleCache {
    return DownloadManagerBuilder.buildCache(context, databaseProvider)
  }

  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  fun provideDownloadManager(
    @ApplicationContext context: Context,
    databaseProvider: StandaloneDatabaseProvider,
    cache: SimpleCache,
    @HttpDataSourceType dataSourceFactory: DataSource.Factory,
  ): DownloadManager {
    return DownloadManagerBuilder.buildDownloadManager(
      context = context,
      cache = cache,
      databaseProvider = databaseProvider,
      dataSourceFactory = dataSourceFactory,
    ).apply {
      maxParallelDownloads = 3
    }
  }

  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  fun provideDownloadController(
    @ApplicationContext context: Context,
    downloadManager: DownloadManager,
  ): DownloadController {
    return DownloadController(context, downloadManager)
  }
}
