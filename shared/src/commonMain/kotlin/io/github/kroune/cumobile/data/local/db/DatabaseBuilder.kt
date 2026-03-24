package io.github.kroune.cumobile.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DatabaseFileName = "cumobile.db"

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    queryDispatcher: CoroutineDispatcher = Dispatchers.IO,
): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryDispatcher)
        .build()
