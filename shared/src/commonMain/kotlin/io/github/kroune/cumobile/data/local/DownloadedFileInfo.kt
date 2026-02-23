package io.github.kroune.cumobile.data.local

import kotlin.math.roundToLong

/**
 * Represents a file downloaded to local storage.
 *
 * Used by the Files tab to display and manage downloaded files.
 */
data class DownloadedFileInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
) {
    /** File extension in uppercase (e.g., "PDF", "DOCX"). */
    val extension: String
        get() = name.substringAfterLast('.', "").uppercase()
}

/**
 * Formats a byte count into a human-readable string (Б, КБ, МБ).
 */
fun formatSizeBytes(bytes: Long): String {
    if (bytes < 1024L) return "$bytes Б"
    val kb = bytes.toDouble() / 1024.0
    if (kb < 1024.0) return "${roundOneDecimal(kb)} КБ"
    val mb = kb / 1024.0
    return "${roundOneDecimal(mb)} МБ"
}

/**
 * Rounds a double to one decimal place and formats as a string.
 */
private fun roundOneDecimal(value: Double): String {
    val rounded = (value * 10).roundToLong()
    val intPart = rounded / 10
    val decPart = rounded % 10
    return "$intPart.$decPart"
}
