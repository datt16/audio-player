package io.github.datt16.audioplayer.core.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
  private val exoPlayer: ExoPlayer,
  private val dataSourceFactory: DataSource.Factory,
) {
  val player
    get() = exoPlayer

  @OptIn(UnstableApi::class)
  fun setup(uri: Uri) {
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
  }

  fun play() {
    exoPlayer.play()
  }
}
