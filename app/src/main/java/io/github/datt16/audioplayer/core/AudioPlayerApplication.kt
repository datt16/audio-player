package io.github.datt16.audioplayer.core

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.github.datt16.audioplayer.BuildConfig
import io.github.datt16.audioplayer.core.player.download.DownloadWorkerFactory
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
@HiltAndroidApp
class AudioPlayerApplication : Application() {
  @Inject
  lateinit var downloadManager: DownloadManager

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    val downloadWorkerFactory = DownloadWorkerFactory(downloadManager)
    val workerConfig = Configuration.Builder()
      .setWorkerFactory(downloadWorkerFactory)
      .build()
    WorkManager.initialize(this, workerConfig)
  }
}
