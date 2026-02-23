package io.github.kroune.cumobile

import android.app.Application
import io.github.kroune.cumobile.di.initKoinAndroid

class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
