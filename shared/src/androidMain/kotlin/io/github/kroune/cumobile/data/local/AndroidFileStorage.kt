package io.github.kroune.cumobile.data.local

import android.content.Context
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of [FileStorage].
 *
 * Stores downloaded files in a `downloads` subdirectory
 * within the app's internal files directory.
 */
internal class AndroidFileStorage(
    context: Context,
) : FileStorage {
    private val downloadsDir: File =
        File(context.filesDir, "downloads").also { it.mkdirs() }

    override fun listFiles(): List<DownloadedFileInfo> {
        val files = downloadsDir.listFiles() ?: return emptyList()
        return files
            .filter { it.isFile }
            .sortedByDescending { it.lastModified() }
            .map { file ->
                DownloadedFileInfo(
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    lastModifiedMillis = file.lastModified(),
                )
            }
    }

    override fun deleteFile(name: String): Boolean {
        val file = resolveSecure(name) ?: return false
        return file.exists() && file.delete()
    }

    override fun deleteAllFiles(): Int {
        val files = downloadsDir.listFiles() ?: return 0
        var count = 0
        for (file in files) {
            if (file.isFile && file.delete()) {
                count++
            }
        }
        return count
    }

    override fun saveFile(
        bytes: ByteArray,
        filename: String,
    ): Boolean =
        try {
            val file = resolveSecure(filename) ?: return false
            file.writeBytes(bytes)
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to save file: $filename" }
            false
        }

    override fun fileExists(name: String): Boolean {
        val file = resolveSecure(name) ?: return false
        return file.exists()
    }

    /**
     * Resolves [name] inside [downloadsDir] and validates that the
     * resolved path does not escape the directory (path traversal guard).
     *
     * @return the resolved [File], or `null` if the name is invalid.
     */
    private fun resolveSecure(name: String): File? {
        val file = File(downloadsDir, name)
        if (!file.canonicalPath.startsWith(downloadsDir.canonicalPath)) {
            logger.warn { "Rejected path-traversal file name: $name" }
            return null
        }
        return file
    }
}
