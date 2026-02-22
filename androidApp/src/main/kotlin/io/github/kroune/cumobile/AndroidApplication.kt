package io.github.kroune.cumobile

import android.app.Application
import io.github.kroune.cumobile.di.initKoin

class AndroidApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin()
  }
}
