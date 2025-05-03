package io.github.datt16.audioplayer.core.data.api

import io.github.datt16.audioplayer.core.data.model.MediaFileLicenseResponse
import io.github.datt16.audioplayer.core.data.model.MediaFilesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface MediaApiService {
  @GET("api/files")
  suspend fun getMediaFiles(): MediaFilesResponse

  @GET("api/media/{mediaId}/license")
  suspend fun getMediaLicense(@Path("mediaId") mediaId: String): MediaFileLicenseResponse
}
