package io.github.kroune.cumobile.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DatabaseFileName = "cumobile.db"

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    driver: SQLiteDriver,
    queryDispatcher: CoroutineDispatcher = Dispatchers.IO,
): AppDatabase =
    builder
        .setDriver(driver)
        .setQueryCoroutineContext(queryDispatcher)
        .build()
