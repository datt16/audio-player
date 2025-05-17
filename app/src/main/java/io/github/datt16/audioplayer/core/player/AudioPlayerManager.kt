package io.github.datt16.audioplayer.core.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.datt16.audioplayer.core.player.datasource.OnMemoryDataSourceProvider
import io.github.datt16.audioplayer.core.player.processor.CustomRenderersFactory

@UnstableApi
class AudioPlayerManager(
  val context: Context
) : LifecycleObserver {

  private var player: ExoPlayer? = null

  fun initialize() {
    player = ExoPlayer.Builder(context /*renderersFactory*/).build()
  }

  fun prepare(uri: Uri, contentId: String) {
    player?.apply {
      val mediaItem = MediaItem.Builder()
        .setUri(uri)
        .setMediaId(contentId)
        .build()

      setMediaSource(
        DefaultMediaSourceFactory(
          OnMemoryDataSourceProvider(uri, contentId)
        ).createMediaSource(mediaItem)
      )
    }
  }

  fun play() {}
  fun stop() {}
  fun release() {}
  fun seekTo(positionMs: Long) {}
  fun fastForward(forwardMs: Long) {}
  fun rewind(rewindMs: Long) {}

  fun lifecycleObserver(): LifecycleObserver = this
}