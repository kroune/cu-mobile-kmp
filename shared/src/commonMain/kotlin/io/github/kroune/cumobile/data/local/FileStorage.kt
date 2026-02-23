package io.github.kroune.cumobile.data.local

/**
 * Abstraction for platform-specific file system operations.
 *
 * Manages downloaded files in the app's local downloads directory.
 * Implementations are provided per platform (Android/iOS) and
 * registered via Koin platform modules.
 */
interface FileStorage {
    /**
     * Lists all downloaded files, sorted by modification date (newest first).
     */
    fun listFiles(): List<DownloadedFileInfo>

    /**
     * Deletes a file by name from the downloads directory.
     * @return true if the file was deleted successfully.
     */
    fun deleteFile(name: String): Boolean

    /**
     * Deletes all files in the downloads directory.
     * @return number of files deleted.
     */
    fun deleteAllFiles(): Int

    /**
     * Saves raw bytes as a file with the given name.
     * Overwrites if a file with the same name already exists.
     * @return true if saved successfully.
     */
    fun saveFile(
        bytes: ByteArray,
        filename: String,
    ): Boolean

    /**
     * Checks if a file with the given name exists.
     */
    fun fileExists(name: String): Boolean
}
