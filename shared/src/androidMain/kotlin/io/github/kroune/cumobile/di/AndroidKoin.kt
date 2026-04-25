package io.github.kroune.cumobile.di

import android.content.Context
import androidx.sqlite.driver.AndroidSQLiteDriver
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.kroune.cumobile.data.local.AndroidFileOpener
import io.github.kroune.cumobile.data.local.AndroidFileStorage
import io.github.kroune.cumobile.data.local.AndroidPdfGenerator
import io.github.kroune.cumobile.data.local.FileOpener
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.PdfGenerator
import io.github.kroune.cumobile.data.local.createDataStore
import io.github.kroune.cumobile.data.local.dataStorePath
import io.github.kroune.cumobile.data.local.db.buildAppDatabase
import io.github.kroune.cumobile.data.local.db.getDatabaseBuilder
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * Initializes Koin with the Android platform module.
 * Called from [io.github.kroune.cumobile.AndroidApplication].
 */
fun initKoinAndroid(context: Context) {
    val platformModule = module {
        single { createDataStore { dataStorePath(context) } }
        single { buildAppDatabase(getDatabaseBuilder(context), AndroidSQLiteDriver()) }
        single<FileStorage> { AndroidFileStorage(context) }
        single<FileOpener> { AndroidFileOpener(context) }
        single<PdfGenerator> { AndroidPdfGenerator() }
    }
    initKoin(platformModule)
    setupCoil(context)
}

@OptIn(coil3.annotation.ExperimentalCoilApi::class)
private fun setupCoil(context: Context) {
    val httpClient = KoinPlatform.getKoin().get<HttpClient>()
    SingletonImageLoader.setSafe { _ ->
        ImageLoader
            .Builder(context)
            .components { add(KtorNetworkFetcherFactory(httpClient)) }
            .build()
    }
}
