package io.github.datt16.audioplayer.viewmodels

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.datt16.audioplayer.core.data.model.MediaFile
import io.github.datt16.audioplayer.core.data.repository.MediaRepository
import io.github.datt16.audioplayer.core.player.util.AudioLevelManager
import io.github.datt16.audioplayer.core.player.ExoPlayerPlaybackManagerOld
import io.github.datt16.audioplayer.core.player.download.DownloadController
import io.github.datt16.audioplayer.core.player.download.DownloadStatus
import io.github.datt16.audioplayer.core.player.util.checkMediaDownloaded
import io.github.datt16.audioplayer.core.player.util.decryptFileEncryptedByAesCBC
import io.github.datt16.audioplayer.core.player.util.getDownloadedFile
import io.github.datt16.audioplayer.screens.home.HomeUiState
import io.github.datt16.audioplayer.screens.home.MediaFileItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


private data class HomeViewModelState(
  val isLoading: Boolean = false,
  val mediaFiles: List<MediaFile> = emptyList(),
  val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
  private val playbackManager: ExoPlayerPlaybackManagerOld,
  audioLevelManager: AudioLevelManager,
  private val mediaRepository: MediaRepository,
  private val mediaCache: SimpleCache,
  private val downloadController: DownloadController,
) : ViewModel() {

  private val viewModelState = MutableStateFlow(HomeViewModelState())
  private val downloadStatusMap = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())

  val uiState by lazy {
    combine(viewModelState, downloadStatusMap) { vmState, progressMap ->
      when {
        vmState.isLoading -> HomeUiState.Loading
        vmState.errorMessage != null -> {
          HomeUiState.Error(
            vmState.errorMessage,
            sampleMediaFiles.map { MediaFileItemState.Loaded(it, false) }
          )
        }

        else -> {
          HomeUiState.Success(
            mediaFiles = vmState.mediaFiles.map {
              when (val downloadStatus = progressMap[it.mediaId]) {
                null, is DownloadStatus.Enqueued -> MediaFileItemState.NotLoaded(mediaFile = it)
                is DownloadStatus.Downloading -> MediaFileItemState.Loading(
                  mediaFile = it,
                  status = downloadStatus
                )

                is DownloadStatus.Failed, is DownloadStatus.Success -> {
                  MediaFileItemState.Loaded(
                    mediaFile = it,
                    isFailed = downloadStatus is DownloadStatus.Failed,
                  )
                }
              }
            }
          )
        }
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = HomeUiState.Loading,
    )
  }

  val duration
    get() = playbackManager.duration
  val playbackFlow = playbackManager.playbackProgressFlow

  val audioLevelFlow = audioLevelManager.audioLevelFlow

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  fun startPlayback(mediaFile: MediaFile) {
    viewModelScope.launch {
      try {
        val url = if (mediaFile.mediaId.startsWith("sample")) {
          mediaFile.playableUrl
        } else {
          "http://10.0.2.2:8888/api/media/${mediaFile.mediaId}"
        }

        if (mediaFile.isEncrypted) {
          preparePlaybackForEncryptedMedia(mediaFile = mediaFile, mediaUrl = url)
        } else {
          downloadStatusMap.update { currentProgressMap ->
            currentProgressMap + (mediaFile.mediaId to DownloadStatus.Success(mediaFile.mediaId))
          }
          playbackManager.setup(url.toUri())
        }

        playbackManager.play()
        _isPlaying.value = true
      } catch (e: Exception) {
        // エラーハンドリング
        _isPlaying.value = false
      }
    }
  }

  private suspend fun preparePlaybackForEncryptedMedia(mediaFile: MediaFile, mediaUrl: String) {
    try {
      if (!mediaCache.checkMediaDownloaded(mediaFile.mediaId)) {
        // ファイルのダウンロードが終わってなかったらダウンロード呼び出して再生準備はしない
        downloadMediaFile(mediaFile.mediaId, mediaUrl)
        return
      } else {
        downloadStatusMap.update { currentProgressMap ->
          currentProgressMap + (mediaFile.mediaId to DownloadStatus.Success(mediaFile.mediaId))
        }
      }

      val key = mediaRepository.getMediaLicense(mediaFile.mediaId).getOrThrow()
      val iv = mediaFile.iv
        ?: throw IllegalArgumentException("iv is required for playback encrypted media file")

      // キャッシュにある暗号化データを取得し復号する
      val encryptedFile = mediaCache.getDownloadedFile(mediaFile.mediaId)
      val currentDecryptedData = decryptFileEncryptedByAesCBC(encryptedFile, key, iv)

      // 復号結果のバイト配列データで再生開始
      playbackManager.setup(mediaFileByteArray = currentDecryptedData)
    } catch (e: Exception) {
      _isPlaying.value = false
      Timber.tag("EncryptedPlayback").e(e)
    }
  }

  private fun downloadMediaFile(mediaId: String, mediaUrl: String) {
    downloadController.startDownload(mediaId, mediaUrl)
    // メソッド呼ばれるたびにスコープ作られてリークするかも (完了前に呼ばれた場合とか)
    val downloaderScope = CoroutineScope(Dispatchers.IO)

    downloadController.getDownloadProgressFlow(mediaId).onEach { downloadStatus ->
      downloadStatusMap.update { currentProgressMap -> currentProgressMap + (mediaId to downloadStatus) }
    }.onCompletion {
      downloaderScope.cancel()
    }.catch {
      downloadStatusMap.update { currentMap ->
        currentMap + (mediaId to DownloadStatus.Failed(mediaId, WorkInfo.State.FAILED))
      }
    }.launchIn(downloaderScope)
  }

  fun play() {
    _isPlaying.value = true
    playbackManager.play()
  }

  fun pause() {
    _isPlaying.value = false
    playbackManager.pause()
  }

  fun seekTo(percentage: Float) {
    playbackManager.seekToByPercentage(percentage)
  }

  fun fetchMediaFiles() {
    viewModelScope.launch {
      viewModelState.update { it.copy(isLoading = true) }
      mediaRepository
        .getMediaFiles()
        .onSuccess { files ->
          viewModelState.update { it.copy(mediaFiles = files, isLoading = false) }
        }
        .onFailure { error ->
          viewModelState.update {
            it.copy(
              errorMessage = error.message ?: "不明なエラーが発生しました",
              isLoading = false,
            )
          }
        }
    }
  }

  companion object {
    val sampleMediaFiles = listOf(
      MediaFile(
        name = "sample01(オフライン再生用)",
        path = "",
        size = 0,
        type = "audio",
        isDir = false,
        relativePath = "",
        mediaId = "sample01",
        playableUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        iv = null,
        isEncrypted = false,
      )
    )
  }
}
