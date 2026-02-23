package io.github.kroune.cumobile.data.local

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of [FileStorage].
 *
 * Stores downloaded files in a `downloads` subdirectory
 * within the app's Documents directory.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosFileStorage : FileStorage {
    private val fileManager = NSFileManager.defaultManager

    private val downloadsDir: String by lazy {
        val documentsUrl = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val url = fileManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = errorPtr.ptr,
            )
            errorPtr.value?.let {
                logger.error { "Failed to locate Documents dir: ${it.localizedDescription}" }
            }
            url
        }
        val path = requireNotNull(documentsUrl).path!! + "/downloads"
        if (!fileManager.fileExistsAtPath(path)) {
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                val success = fileManager.createDirectoryAtPath(
                    path = path,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = errorPtr.ptr,
                )
                if (!success) {
                    val error = errorPtr.value
                    logger.error {
                        "Failed to create downloads dir: ${error?.localizedDescription}"
                    }
                }
            }
        }
        path
    }

    override fun listFiles(): List<DownloadedFileInfo> {
        val contents = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val result = fileManager.contentsOfDirectoryAtPath(
                downloadsDir,
                error = errorPtr.ptr,
            )
            errorPtr.value?.let {
                logger.error { "Failed to list downloads dir: ${it.localizedDescription}" }
            }
            result
        } ?: return emptyList()

        return contents
            .filterIsInstance<String>()
            .mapNotNull { filename -> buildFileInfo(filename) }
            .sortedByDescending { it.lastModifiedMillis }
    }

    private fun buildFileInfo(filename: String): DownloadedFileInfo? {
        val fullPath = nsString(downloadsDir)
            .stringByAppendingPathComponent(filename)
        val attributes = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val attrs = fileManager.attributesOfItemAtPath(
                fullPath,
                error = errorPtr.ptr,
            )
            errorPtr.value?.let {
                logger.warn { "Failed to read attributes for $filename: ${it.localizedDescription}" }
            }
            attrs
        } ?: return null

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
        val path = resolveSecure(name) ?: return false
        return memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val success = fileManager.removeItemAtPath(path, error = errorPtr.ptr)
            if (!success) {
                errorPtr.value?.let {
                    logger.error { "Failed to delete $name: ${it.localizedDescription}" }
                }
            }
            success
        }
    }

    override fun deleteAllFiles(): Int {
        val contents = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val result = fileManager.contentsOfDirectoryAtPath(
                downloadsDir,
                error = errorPtr.ptr,
            )
            errorPtr.value?.let {
                logger.error { "Failed to list dir for deleteAll: ${it.localizedDescription}" }
            }
            result
        } ?: return 0

        var count = 0
        for (item in contents.filterIsInstance<String>()) {
            val path = nsString(downloadsDir)
                .stringByAppendingPathComponent(item)
            val deleted = memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                fileManager.removeItemAtPath(path, error = errorPtr.ptr)
            }
            if (deleted) {
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
            val path = resolveSecure(filename) ?: return false
            val nsData = bytes.toNSData()
            nsData.writeToFile(path, atomically = true)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save file: $filename" }
            false
        }

    override fun fileExists(name: String): Boolean {
        val path = resolveSecure(name) ?: return false
        return fileManager.fileExistsAtPath(path)
    }

    /**
     * Resolves [name] inside [downloadsDir] using Foundation path
     * APIs and validates that the result does not escape the directory
     * (path traversal guard).
     *
     * @return the resolved path, or `null` if the name is invalid.
     */
    private fun resolveSecure(name: String): String? {
        val resolved = nsString(downloadsDir)
            .stringByAppendingPathComponent(name)
        if (!resolved.startsWith(downloadsDir)) {
            logger.warn { "Rejected path-traversal file name: $name" }
            return null
        }
        return resolved
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
