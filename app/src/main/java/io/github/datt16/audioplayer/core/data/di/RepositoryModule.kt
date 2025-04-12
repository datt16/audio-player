package io.github.datt16.audioplayer.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.data.repository.MediaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  @Singleton
  abstract fun bindMediaRepository(mediaRepositoryImpl: MediaRepositoryImpl): MediaRepository
}
