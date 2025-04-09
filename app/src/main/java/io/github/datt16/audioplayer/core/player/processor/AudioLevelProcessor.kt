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
import kotlin.math.pow

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

  // 音量マッピングのパラメータ
  // 会話の音量を0.3〜0.8にマッピングするための調整
  var gainAmplification: Float = 4.0f // ゲイン増幅係数（大きいほど小さな音も拾う）
  var gainExponent: Float = 0.6f // 指数（1未満だと小さな音が強調される）
  var minOutputLevel: Float = 0.0f // 出力の最小値
  var maxOutputLevel: Float = 1.0f // 出力の最大値
  var midBoostThreshold: Float = 0.3f // 中音量強調の閾値

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
    var rawLevel = sum / (sampleCount * channelCount)

    // 音量レベルの非線形変換を適用
    // 1. ゲイン増幅を適用（小さな音も拾えるように）
    var adjustedLevel = (rawLevel * gainAmplification).coerceAtMost(1.0f)

    // 2. 指数関数で非線形マッピング（小音量を強調）
    adjustedLevel = adjustedLevel.pow(gainExponent)

    // 3. 出力範囲の調整（最小値を0.0ではなく設定値に）
    adjustedLevel = minOutputLevel + (maxOutputLevel - minOutputLevel) * adjustedLevel

    // 4. 中音量の強調（会話レベルを0.3-0.8の範囲に押し上げる）
    if (adjustedLevel > midBoostThreshold && adjustedLevel < 0.8f) {
      // 0.3〜0.8の範囲を、より高い0.3〜0.8の範囲に再マッピング
      val boostFactor = 0.5f / (0.8f - midBoostThreshold) // 中音量範囲を0.5の幅に圧縮
      adjustedLevel = midBoostThreshold + (adjustedLevel - midBoostThreshold) * boostFactor
    }

    // 平滑化（滑らかに変化させる）
    _currentLevel = _currentLevel * (1 - smoothingFactor) + adjustedLevel * smoothingFactor

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
