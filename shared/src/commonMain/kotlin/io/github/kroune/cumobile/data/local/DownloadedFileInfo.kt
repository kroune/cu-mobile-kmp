package io.github.kroune.cumobile.data.local

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
