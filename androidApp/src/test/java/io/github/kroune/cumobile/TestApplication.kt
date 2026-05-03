package io.github.kroune.cumobile

import android.app.Application
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.test.FakeImageLoaderEngine

class TestApplication : Application() {
    @OptIn(coil3.annotation.ExperimentalCoilApi::class)
    override fun onCreate() {
        super.onCreate()
        val engine = FakeImageLoaderEngine
            .Builder()
            .default(ColorImage())
            .build()
        SingletonImageLoader.setSafe {
            ImageLoader
                .Builder(this)
                .components { add(engine) }
                .build()
        }
    }
}
