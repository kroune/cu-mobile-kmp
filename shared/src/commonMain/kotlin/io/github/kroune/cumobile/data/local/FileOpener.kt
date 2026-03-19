package io.github.kroune.cumobile.data.local

/**
 * Platform-specific file opener.
 *
 * Opens a downloaded file using the system's default handler
 * (e.g., Intent.ACTION_VIEW on Android, UIDocumentInteractionController on iOS).
 */
interface FileOpener {
    /**
     * Opens the file at the given absolute [path].
     *
     * @return `true` if the file was opened successfully.
     */
    fun openFile(path: String): Boolean
}
