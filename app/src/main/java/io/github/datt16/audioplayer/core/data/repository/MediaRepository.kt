package io.github.datt16.audioplayer.core.data.repository

import io.github.datt16.audioplayer.core.data.model.MediaFile

interface MediaRepository {
  suspend fun getMediaFiles(): Result<List<MediaFile>>
  suspend fun getMediaLicense(mediaId: String): Result<String>
}
