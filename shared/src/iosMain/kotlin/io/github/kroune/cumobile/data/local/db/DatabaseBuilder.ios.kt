package io.github.kroune.cumobile.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private val logger = KotlinLogging.logger {}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = appSupportDirectory() + "/$DatabaseFileName"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun appSupportDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val url = memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val result = fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = errorPtr.ptr,
        )
        errorPtr.value?.let {
            logger.error { "Failed to locate Application Support dir: ${it.localizedDescription}" }
        }
        result
    }
    return requireNotNull(url?.path) {
        "Could not resolve Application Support directory"
    }
}
