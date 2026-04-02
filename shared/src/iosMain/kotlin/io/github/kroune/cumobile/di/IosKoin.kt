package io.github.kroune.cumobile.di

import androidx.sqlite.driver.NativeSQLiteDriver
import io.github.kroune.cumobile.data.local.FileOpener
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.IosFileOpener
import io.github.kroune.cumobile.data.local.IosFileStorage
import io.github.kroune.cumobile.data.local.IosPdfGenerator
import io.github.kroune.cumobile.data.local.PdfGenerator
import io.github.kroune.cumobile.data.local.createDataStore
import io.github.kroune.cumobile.data.local.dataStorePath
import io.github.kroune.cumobile.data.local.db.buildAppDatabase
import io.github.kroune.cumobile.data.local.db.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing platform dependencies (DataStore, FileStorage).
 * Called from Swift via `IosKoinKt.iosKoinModule()`.
 */
fun iosKoinModule(): Module =
    module {
        single { createDataStore { dataStorePath() } }
        single { buildAppDatabase(getDatabaseBuilder(), NativeSQLiteDriver()) }
        single<FileStorage> { IosFileStorage() }
        single<FileOpener> { IosFileOpener() }
        single<PdfGenerator> { IosPdfGenerator() }
    }
