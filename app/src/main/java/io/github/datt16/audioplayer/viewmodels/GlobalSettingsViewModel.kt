package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.player.download.DownloadController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
  private val downloadController: DownloadController,
  private val simpleCache: SimpleCache,
) : ViewModel() {
  val downloadProgress = downloadController.getAllDownloadProgressMapFlow()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = null,
    )

  private val _cacheItems = MutableStateFlow<List<CacheItem>>(emptyList())
  val cacheItems = _cacheItems.asStateFlow()

  fun startDownloadSample() {
    startDownload("sample01", "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
  }

  private fun startDownload(contentId: String, contentUrl: String) {
    downloadController.startDownload(contentId, contentUrl)
  }

  fun pauseDownload(contentId: String) {
    downloadController.pauseDownload(contentId)
  }

  fun cancelDownload(contentId: String) {
    downloadController.cancelDownload(contentId)
  }

  fun checkCacheEntries() {
    val entries = simpleCache.keys.flatMap { key ->
      simpleCache.getCachedSpans(key).mapNotNull { span ->
        span.file?.let { file ->
          CacheItem(
            key = key,
            path = file.absolutePath,
            sizeMb = span.length.toDouble() / 1024 / 1024,
          )
        }
      }
    }

    _cacheItems.value = entries
  }
}

data class CacheItem(
  val key: String,
  val path: String,
  val sizeMb: Double,
)
