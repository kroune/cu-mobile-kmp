package io.github.kroune.cumobile

import android.app.Application
import io.github.kroune.cumobile.di.initKoinAndroid

class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty("kotlin-logging-to-android-native", "true")
        initKoinAndroid(this)
    }
}
