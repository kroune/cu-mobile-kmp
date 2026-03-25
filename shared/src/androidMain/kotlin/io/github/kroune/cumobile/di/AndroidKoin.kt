package io.github.kroune.cumobile.di

import android.content.Context
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
import org.koin.dsl.module

/**
 * Initializes Koin with the Android platform module.
 * Called from [io.github.kroune.cumobile.AndroidApplication].
 */
fun initKoinAndroid(context: Context) {
    val platformModule = module {
        single { createDataStore { dataStorePath(context) } }
        single { buildAppDatabase(getDatabaseBuilder(context)) }
        single<FileStorage> { AndroidFileStorage(context) }
        single<FileOpener> { AndroidFileOpener(context) }
        single<PdfGenerator> { AndroidPdfGenerator() }
    }
    initKoin(platformModule)
}
