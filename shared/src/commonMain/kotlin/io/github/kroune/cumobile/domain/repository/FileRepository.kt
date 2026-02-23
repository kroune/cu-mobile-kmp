package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.local.DownloadedFileInfo

/**
 * Repository for managing locally downloaded files.
 *
 * Combines [FileStorage] for disk operations and [HttpClient]
 * for downloading content from pre-signed URLs.
 */
interface FileRepository {
    /**
     * Lists all downloaded files, sorted by modification date (newest first).
     */
    suspend fun listDownloadedFiles(): List<DownloadedFileInfo>

    /**
     * Deletes a single file by name.
     * @return true if deleted successfully.
     */
    suspend fun deleteFile(name: String): Boolean

    /**
     * Deletes all downloaded files.
     * @return number of files deleted.
     */
    suspend fun deleteAllFiles(): Int

    /**
     * Downloads a file from the given URL and saves it locally.
     *
     * @param url pre-signed download URL (absolute).
     * @param filename local filename to save as.
     * @return true if download and save succeeded.
     */
    suspend fun downloadAndSave(
        url: String,
        filename: String,
    ): Boolean

    /**
     * Checks if a file with the given name is already downloaded.
     */
    suspend fun fileExists(name: String): Boolean
}
