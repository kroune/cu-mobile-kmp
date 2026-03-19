package io.github.kroune.cumobile

import android.app.Application
import io.github.kroune.cumobile.di.initKoinAndroid
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty("kotlin-logging-to-android-native", "true")
        KotlinLoggingConfiguration.direct.logLevel = Level.DEBUG
        initKoinAndroid(this)
    }
}
