package io.github.datt16.audioplayer.core.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.datt16.audioplayer.core.data.api.MediaApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
  @Provides
  @Singleton
  fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  @Provides
  @Singleton
  fun provideMediaApiService(json: Json): MediaApiService {
    return Retrofit.Builder()
      .baseUrl("http://10.0.2.2:8888/")
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()
      .create(MediaApiService::class.java)
  }
}
