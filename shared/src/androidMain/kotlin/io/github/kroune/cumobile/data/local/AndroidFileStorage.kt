package io.github.kroune.cumobile.data.local

import android.content.Context
import java.io.File

/**
 * Android implementation of [FileStorage].
 *
 * Stores downloaded files in a `downloads` subdirectory
 * within the app's internal files directory.
 */
class AndroidFileStorage(
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
        val file = File(downloadsDir, name)
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
            val file = File(downloadsDir, filename)
            file.writeBytes(bytes)
            true
        } catch (_: Exception) {
            false
        }

    override fun fileExists(name: String): Boolean = File(downloadsDir, name).exists()
}
