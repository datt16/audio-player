package io.github.datt16.abstraction.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.datt16.abstraction.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class AbstractionApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
