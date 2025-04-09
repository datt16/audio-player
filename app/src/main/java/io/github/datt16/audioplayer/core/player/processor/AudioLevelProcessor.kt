package io.github.datt16.audioplayer.core.player.processor

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** オーディオレベル（ゲイン）を監視するためのオーディオプロセッサー 元のオーディオデータは変更せず、音量レベル情報のみを抽出します */
@Singleton
@OptIn(UnstableApi::class)
class AudioLevelProcessor @Inject constructor() : AudioProcessor {
  private var inputAudioFormat: AudioProcessor.AudioFormat = AudioProcessor.AudioFormat.NOT_SET
  private var outputAudioFormat: AudioProcessor.AudioFormat = AudioProcessor.AudioFormat.NOT_SET
  private var buffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
  private var inputEnded: Boolean = false

  // 現在の平均音量レベル（0.0f〜1.0f）
  private var _currentLevel: Float = 0.0f

  // 最近計算された音量レベル（0.0f〜1.0f）
  val currentLevel: Float
    get() = _currentLevel

  // 平滑化の係数（0.0f〜1.0f）：値が小さいほど滑らかに変化
  var smoothingFactor: Float = 0.1f

  // 音量レベルが変更された時のリスナー
  var onLevelChanged: ((level: Float) -> Unit)? = null

  override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
    // サポートするフォーマットを確認（16ビットPCMのみサポート）
    if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
      throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
    }

    this.inputAudioFormat = inputAudioFormat
    this.outputAudioFormat = inputAudioFormat // 出力フォーマットは変更しない
    return outputAudioFormat
  }

  override fun isActive(): Boolean {
    // このプロセッサーは常にアクティブ（オーディオレベルを計算するため）
    return outputAudioFormat != AudioProcessor.AudioFormat.NOT_SET
  }

  override fun queueInput(inputBuffer: ByteBuffer) {
    if (!inputBuffer.hasRemaining()) {
      return
    }

    // 入力バッファの内容をコピー
    val inputBytes = ByteArray(inputBuffer.remaining())
    val initialPosition = inputBuffer.position()
    inputBuffer.get(inputBytes)

    // 元の位置に戻して、データを変更せずにそのまま出力
    inputBuffer.position(initialPosition)
    buffer = inputBuffer

    // 音量レベルを計算
    calculateAudioLevel(inputBytes)
  }

  private fun calculateAudioLevel(bytes: ByteArray) {
    if (inputAudioFormat == AudioProcessor.AudioFormat.NOT_SET) return

    val channelCount = inputAudioFormat.channelCount
    val bytesPerSample = 2 // 16ビットPCM = 2バイト
    val sampleCount = bytes.size / (bytesPerSample * channelCount)

    if (sampleCount == 0) return

    var sum = 0.0f
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val shortBuffer = buffer.asShortBuffer()

    // 各サンプルの絶対値の平均を計算
    for (i in 0 until sampleCount * channelCount) {
      val sample = shortBuffer.get()
      // Shortの範囲は-32768〜32767なので、最大値の32768.0fで割って正規化
      sum += abs(sample.toFloat()) / 32768.0f
    }

    // 全チャンネルの平均音量を計算
    val newLevel = sum / (sampleCount * channelCount)

    // 平滑化（滑らかに変化させる）
    _currentLevel = _currentLevel * (1 - smoothingFactor) + newLevel * smoothingFactor

    // リスナーがあれば呼び出す
    onLevelChanged?.invoke(_currentLevel)
  }

  override fun queueEndOfStream() {
    inputEnded = true
  }

  override fun getOutput(): ByteBuffer {
    val output = buffer
    buffer = AudioProcessor.EMPTY_BUFFER
    return output
  }

  override fun isEnded(): Boolean {
    return inputEnded && buffer === AudioProcessor.EMPTY_BUFFER
  }

  override fun flush() {
    buffer = AudioProcessor.EMPTY_BUFFER
    inputEnded = false
  }

  override fun reset() {
    flush()
    outputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
    inputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
    _currentLevel = 0.0f
  }
}
