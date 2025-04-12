package io.github.datt16.audioplayer.core.data.api

import io.github.datt16.audioplayer.core.data.model.MediaFilesResponse
import retrofit2.http.GET

interface MediaApiService {
  @GET("api/files")
  suspend fun getMediaFiles(): MediaFilesResponse
}
