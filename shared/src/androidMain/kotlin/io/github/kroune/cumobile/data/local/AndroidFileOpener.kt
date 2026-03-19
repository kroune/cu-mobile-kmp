package io.github.kroune.cumobile.data.local

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of [FileOpener].
 *
 * Uses [FileProvider] to create a content URI and [Intent.ACTION_VIEW]
 * to open the file with the system's default handler.
 */
internal class AndroidFileOpener(
    private val context: Context,
) : FileOpener {
    override fun openFile(path: String): Boolean {
        val file = File(path)
        if (!file.exists()) {
            logger.warn { "File does not exist: $path" }
            return false
        }
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = guessMimeType(file.extension)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to open file: $path" }
            false
        }
    }
}

private fun guessMimeType(extension: String): String =
    when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "txt" -> "text/plain"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "mp4" -> "video/mp4"
        "zip" -> "application/zip"
        "html", "htm" -> "text/html"
        else -> "application/octet-stream"
    }
