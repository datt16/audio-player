package io.github.datt16.audioplayer.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.player.AudioLevelManager
import io.github.datt16.audioplayer.core.player.CustomRenderersFactory
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.download.DownloadManagerBuilder
import io.github.datt16.audioplayer.core.player.processor.AudioLevelProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
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
  fun provideCache(
    @ApplicationContext context: Context,
    databaseProvider: StandaloneDatabaseProvider,
  ): SimpleCache {
    return DownloadManagerBuilder.buildCache(context, databaseProvider)
  }

  // --- player
  @Provides
  @Singleton
  @HttpDataSourceType
  fun provideDataSourceFactory(): DataSource.Factory {
    return DefaultHttpDataSource.Factory()
  }

  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  @CacheDataSourceType
  fun provideCacheDataSourceFactory(
    simpleCache: SimpleCache,
    @HttpDataSourceType remoteDataSourceFactory: DataSource.Factory,
  ): DataSource.Factory {
    return CacheDataSource.Factory()
      .setCache(simpleCache)
      .setUpstreamDataSourceFactory(remoteDataSourceFactory)
      .setCacheWriteDataSinkFactory(null)
  }

  @Provides
  @Singleton
  fun provideAudioLevelProcessor(): AudioLevelProcessor {
    return AudioLevelProcessor().apply { smoothingFactor = 0.2f }
  }

  @Provides
  @Singleton
  fun provideAudioLevelManager(audioLevelProcessor: AudioLevelProcessor): AudioLevelManager {
    return AudioLevelManager(audioLevelProcessor)
  }

  @OptIn(UnstableApi::class)
  @Provides
  @Singleton
  fun provideExoPlayer(
    @ApplicationContext context: Context,
    audioLevelProcessor: AudioLevelProcessor,
    @CacheDataSourceType cacheDataSourceFactory: DataSource.Factory,
  ): ExoPlayer {
    val renderersFactory = CustomRenderersFactory(context, audioLevelProcessor)
    return ExoPlayer.Builder(context, renderersFactory).setMediaSourceFactory(
      DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
    ).build()
  }

  // --- download ---
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
