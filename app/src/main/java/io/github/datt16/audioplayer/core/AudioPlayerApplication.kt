package io.github.datt16.audioplayer.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.datt16.audioplayer.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class AudioPlayerApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
