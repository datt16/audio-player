package io.github.datt16.audioplayer.core.player.processor

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import javax.inject.Inject

@OptIn(UnstableApi::class)
class CustomRenderersFactory
@Inject
constructor(context: Context, private val audioLevelProcessor: AudioLevelProcessor) :
  DefaultRenderersFactory(context) {

  override fun buildAudioSink(
    context: Context,
    enableFloatOutput: Boolean,
    enableAudioTrackPlaybackParams: Boolean,
  ): AudioSink {
    return buildCustomAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
  }

  private fun buildCustomAudioSink(
    context: Context,
    enableFloatOutput: Boolean,
    enableAudioTrackPlaybackParams: Boolean,
  ): AudioSink {
    return DefaultAudioSink.Builder(context)
      .setAudioProcessors(arrayOf(audioLevelProcessor))
      .setEnableFloatOutput(enableFloatOutput)
      .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
      .build()
  }
}
