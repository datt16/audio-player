package io.github.datt16.audioplayer.core.player.di

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}