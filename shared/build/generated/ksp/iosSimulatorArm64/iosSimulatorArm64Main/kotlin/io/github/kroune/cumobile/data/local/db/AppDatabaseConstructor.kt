package io.github.kroune.cumobile.`data`.local.db

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase =
        error("Room KSP stub — run iOS build on macOS for real implementation")
}
