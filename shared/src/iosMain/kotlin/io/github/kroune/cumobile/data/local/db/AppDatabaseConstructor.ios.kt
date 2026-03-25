package io.github.kroune.cumobile.data.local.db

import androidx.room.RoomDatabaseConstructor

// Stub for Linux CI compilation. KSP generates the real implementation on Mac.
actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase =
        error("Room KSP-generated implementation expected — run iOS build on macOS")
}
