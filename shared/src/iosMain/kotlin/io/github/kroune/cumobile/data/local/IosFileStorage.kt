package io.github.kroune.cumobile.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile

/**
 * iOS implementation of [FileStorage].
 *
 * Stores downloaded files in a `downloads` subdirectory
 * within the app's Documents directory.
 */
@OptIn(ExperimentalForeignApi::class)
class IosFileStorage : FileStorage {
    private val fileManager = NSFileManager.defaultManager

    private val downloadsDir: String by lazy {
        val documentsUrl = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(documentsUrl).path!! + "/downloads"
        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        path
    }

    override fun listFiles(): List<DownloadedFileInfo> {
        val contents = fileManager.contentsOfDirectoryAtPath(
            downloadsDir,
            error = null,
        ) ?: return emptyList()

        return contents
            .filterIsInstance<String>()
            .mapNotNull { filename -> buildFileInfo(filename) }
            .sortedByDescending { it.lastModifiedMillis }
    }

    private fun buildFileInfo(filename: String): DownloadedFileInfo? {
        val fullPath = nsString(downloadsDir)
            .stringByAppendingPathComponent(filename)
        val attributes = fileManager.attributesOfItemAtPath(
            fullPath,
            error = null,
        ) ?: return null

        val size = (attributes[NSFileSize] as? Number)?.toLong() ?: 0L
        val modDate = attributes[NSFileModificationDate]
        val modMillis = if (modDate is platform.Foundation.NSDate) {
            (modDate.timeIntervalSince1970 * 1000).toLong()
        } else {
            0L
        }

        return DownloadedFileInfo(
            name = filename,
            path = fullPath,
            sizeBytes = size,
            lastModifiedMillis = modMillis,
        )
    }

    override fun deleteFile(name: String): Boolean {
        val path = nsString(downloadsDir)
            .stringByAppendingPathComponent(name)
        return fileManager.removeItemAtPath(path, error = null)
    }

    override fun deleteAllFiles(): Int {
        val contents = fileManager.contentsOfDirectoryAtPath(
            downloadsDir,
            error = null,
        ) ?: return 0

        var count = 0
        for (item in contents.filterIsInstance<String>()) {
            val path = nsString(downloadsDir)
                .stringByAppendingPathComponent(item)
            if (fileManager.removeItemAtPath(path, error = null)) {
                count++
            }
        }
        return count
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun saveFile(
        bytes: ByteArray,
        filename: String,
    ): Boolean =
        try {
            val path = nsString(downloadsDir)
                .stringByAppendingPathComponent(filename)
            val nsData = bytes.toNSData()
            nsData.writeToFile(path, atomically = true)
        } catch (_: Exception) {
            false
        }

    override fun fileExists(name: String): Boolean {
        val path = nsString(downloadsDir)
            .stringByAppendingPathComponent(name)
        return fileManager.fileExistsAtPath(path)
    }

    private fun nsString(value: String): NSString = NSString.create(string = value)
}

/**
 * Converts a Kotlin [ByteArray] to [platform.Foundation.NSData].
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): platform.Foundation.NSData {
    if (isEmpty()) return platform.Foundation.NSData()
    return kotlinx.cinterop.memScoped {
        val pinned = this@toNSData
        platform.Foundation.NSData.create(
            bytes = kotlinx.cinterop.allocArrayOf(pinned),
            length = pinned.size.toULong(),
        )
    }
}
