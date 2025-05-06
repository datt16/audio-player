package io.github.datt16.audioplayer.core.player.util

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
fun SimpleCache.checkMediaDownloaded(mediaId: String): Boolean {
  return getCachedLength(mediaId, 0, Long.MAX_VALUE) > 0
}

@UnstableApi
fun SimpleCache.getDownloadedFile(mediaId: String): File {
  val spans = getCachedSpans(mediaId)
  val span = spans.firstOrNull() ?: throw IllegalStateException("Cached file not found")
  return span.file ?: throw IllegalStateException("Cached file not found")
}
