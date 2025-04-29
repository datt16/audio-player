package io.github.datt16.audioplayer.core.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.datt16.audioplayer.core.player.di.CacheDataSourceType
import io.github.datt16.audioplayer.core.player.di.HttpDataSourceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ExoPlayerPlaybackManager @Inject constructor(
  private val exoPlayer: ExoPlayer,
  @CacheDataSourceType private val dataSourceFactory: DataSource.Factory,
) : Player.Listener, PlaybackManager {

  @OptIn(UnstableApi::class)
  override suspend fun setup(uri: Uri) {
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.addListener(this)
    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
  }

  override fun play() {
    exoPlayer.play()
  }

  override fun pause() {
    exoPlayer.pause()
  }

  override fun seekToByPercentage(playbackPercentage: Float) {
    exoPlayer.seekTo((exoPlayer.duration * playbackPercentage).toLong())
  }

  override val playbackProgressFlow: Flow<PlaybackProgress> by lazy {
    getPlaybackProgressFlow()
  }

  private fun getPlaybackProgressFlow(pollInterval: Long = 1000L): Flow<PlaybackProgress> = flow {
    while (true) {
      val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
      val currentPosition = exoPlayer.currentPosition
      val progressPercentage = currentPosition.toFloat() / duration
      emit(Pair(progressPercentage, currentPosition))
      delay(pollInterval)
    }
  }

  override val isPlaying: Boolean
    get() = exoPlayer.isPlaying

  override val duration: Long
    get() = exoPlayer.duration
}
