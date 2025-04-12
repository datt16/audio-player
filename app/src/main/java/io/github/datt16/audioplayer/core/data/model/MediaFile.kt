package io.github.datt16.audioplayer.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class MediaFilesResponse(val files: List<MediaFile>)

@Serializable
data class MediaFile(
  val name: String,
  val path: String,
  val size: Long,
  val type: String,
  @SerialName("is_dir") val isDir: Boolean,
  @SerialName("relative_path") val relativePath: String,
  @SerialName("media_id") val mediaId: String,
  @SerialName("playable_url") val playableUrl: String,
)
