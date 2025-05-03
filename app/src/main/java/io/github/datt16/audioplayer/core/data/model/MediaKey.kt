package io.github.datt16.audioplayer.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaFileLicenseResponse(val keyRes: MediaKey)

@Serializable
data class MediaKey(
  val key: String
)
