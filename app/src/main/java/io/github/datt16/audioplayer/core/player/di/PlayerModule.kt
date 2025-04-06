package io.github.datt16.audioplayer.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.player.MediaPlayerPlaybackManager
import io.github.datt16.audioplayer.core.player.PlaybackManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

  @Provides
  @Singleton
  fun provideExoPlayer(
    @ApplicationContext context: Context,
  ): ExoPlayer {
    return ExoPlayer.Builder(context).build()
  }

  @Provides
  @Singleton
  fun provideDataSourceFactory(
    @ApplicationContext context: Context,
  ): DataSource.Factory {
    return DefaultDataSource.Factory(context)
  }

  @OptIn(UnstableApi::class)
  @Provides
  fun providePlaybackManager(
    @ApplicationContext context: Context
  ): PlaybackManager {
    return MediaPlayerPlaybackManager(context)
  }
}
