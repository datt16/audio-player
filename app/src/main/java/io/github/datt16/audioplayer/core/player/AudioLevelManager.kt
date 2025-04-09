package io.github.datt16.audioplayer.core.player

import io.github.datt16.audioplayer.core.player.processor.AudioLevelProcessor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** オーディオレベルをSharedFlowとして公開するマネージャークラス */
@Singleton
class AudioLevelManager @Inject constructor(audioLevelProcessor: AudioLevelProcessor) {
  // 音量レベルを公開するSharedFlow
  private val _audioLevelFlow = MutableSharedFlow<Float>(replay = 1)
  val audioLevelFlow: SharedFlow<Float> = _audioLevelFlow.asSharedFlow()

  init {
    // AudioProcessorからの音量レベル変更を監視してFlowに流す
    audioLevelProcessor.onLevelChanged = { level -> _audioLevelFlow.tryEmit(level) }
  }
}
