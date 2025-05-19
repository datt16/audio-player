package io.github.datt16.audioplayer.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.AudioPlayerManager
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.processor.AudioLevelProcessor
import io.github.datt16.audioplayer.core.player.processor.CustomRenderersFactory
import io.github.datt16.audioplayer.core.player.util.AudioLevelManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

  // TODO: 多分いらなくなる
  @Provides
  @Singleton
  @HttpDataSourceType
  fun provideDataSourceFactory(): DataSource.Factory {
    return DefaultHttpDataSource.Factory()
  }

  // TODO: 多分いらなくなる
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

  // TODO: 多分いらなくなる
  @Provides
  @Singleton
  fun provideAudioLevelManager(audioLevelProcessor: AudioLevelProcessor): AudioLevelManager {
    return AudioLevelManager(audioLevelProcessor)
  }

  // TODO: 多分いらなくなる
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

  @UnstableApi
  @Provides
  fun provideAudioPlayerManager(
    @ApplicationContext context: Context,
    downloadManager: DownloadController,
    mediaRepository: MediaRepository,
    encryptedFileCache: SimpleCache,
    audioLevelProcessor: AudioLevelProcessor,
  ): AudioPlayerManager {
    return AudioPlayerManager(
      context = context,
      downloadController = downloadManager,
      mediaRepository = mediaRepository,
      encryptedFileCache = encryptedFileCache,
      customRenderersFactory = CustomRenderersFactory(
        context = context,
        audioLevelProcessor = audioLevelProcessor
      )
    )
  }
}
