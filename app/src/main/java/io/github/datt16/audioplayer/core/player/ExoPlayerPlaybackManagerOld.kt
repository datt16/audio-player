package io.github.datt16.audioplayer.core.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import io.github.datt16.audioplayer.core.player.datasource.ByteArrayDataSource
import io.github.datt16.audioplayer.core.player.di.CacheDataSourceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ExoPlayerPlaybackManagerOld @Inject constructor(
  private val exoPlayer: ExoPlayer,
  @CacheDataSourceType private val dataSourceFactory: DataSource.Factory,
) : Player.Listener, PlaybackManagerOld, ByteArrayDataSource.Listener {

  @OptIn(UnstableApi::class)
  override suspend fun setup(uri: Uri) {
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.addListener(this)
    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
  }

  override suspend fun setup(mediaFileByteArray: ByteArray) {
    // TODO: 引数のByteArrayがいつメモリ上から消えるかキャッチアップする
    val byteArrayDataSourceFactory =
      DataSource.Factory { ByteArrayDataSource(mediaFileByteArray, this) }
    val mediaItem = MediaItem.fromUri(Uri.EMPTY)
    val mediaSource =
      DefaultMediaSourceFactory(byteArrayDataSourceFactory).createMediaSource(mediaItem)

    exoPlayer.addListener(this)
    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
  }

  // for gc
  override fun onClose() {
    System.gc()
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
