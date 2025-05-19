package io.github.datt16.audioplayer.core.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.datasource.DataSourceDebugFlags
import io.github.datt16.audioplayer.core.player.datasource.OnMemoryDataSourceProvider
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.processor.CustomRenderersFactory
import javax.inject.Inject

@UnstableApi
class AudioPlayerManager @Inject constructor(
  private val context: Context,
  private val downloadController: DownloadController,
  private val mediaRepository: MediaRepository,
  private val encryptedFileCache: SimpleCache,
  private val customRenderersFactory: CustomRenderersFactory,
) : LifecycleObserver {

  private var player: ExoPlayer? = null

  fun initialize() {
    player = ExoPlayer.Builder(context, customRenderersFactory).build()
  }

  fun prepare(uri: Uri, contentId: String, skipDecryption: Boolean = false) {
    player?.apply {
      val mediaItem = MediaItem.Builder()
        .setUri(uri)
        .setMediaId(contentId)
        .build()

      setMediaSource(
        DefaultMediaSourceFactory(
          OnMemoryDataSourceProvider(
            contentUri = uri,
            contentId = contentId,
            downloadController = downloadController,
            mediaRepository = mediaRepository,
            encryptedFileCache = encryptedFileCache,
            debugFlags = DataSourceDebugFlags(
              skipDecryptStep = skipDecryption
            )
          )
        ).createMediaSource(mediaItem)
      )
    }
  }

  fun play() {
    player?.playWhenReady = true
    player?.prepare()
  }

  fun stop() {
    player?.playWhenReady = false
  }

  fun release() {
    player = null
  }

  fun seekTo(positionMs: Long) {
    player?.seekTo(positionMs)
  }

  fun fastForward(forwardMs: Long) {
    player?.seekTo(player?.currentPosition?.plus(forwardMs) ?: 0)
  }

  fun rewind(rewindMs: Long) {
    player?.seekTo(player?.currentPosition?.minus(rewindMs) ?: 0)
  }

  fun lifecycleObserver(): LifecycleObserver = this
}
