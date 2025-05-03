package io.github.datt16.audioplayer.core.data.repository

import io.github.datt16.audioplayer.core.data.api.MediaApiService
import io.github.datt16.audioplayer.core.data.model.MediaFile
import timber.log.Timber
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(private val apiService: MediaApiService) :
  MediaRepository {
  override suspend fun getMediaFiles(): Result<List<MediaFile>> =
    runCatching { apiService.getMediaFiles().files }.onFailure {
      Timber.e(it, "Failed to fetch media files")
    }

  override suspend fun getMediaLicense(mediaId: String): Result<String> =
    runCatching { apiService.getMediaLicense(mediaId).keyRes.key }.onFailure {
      Timber.e(it, "Failed to fetch media license")
    }
}
