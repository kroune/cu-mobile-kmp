package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation of [FileRepository].
 *
 * Uses [FileStorage] for local disk operations and [HttpClient]
 * for downloading files from pre-signed URLs.
 */
class FileRepositoryImpl(
    private val fileStorage: FileStorage,
    private val httpClient: HttpClient,
) : FileRepository {
    override suspend fun listDownloadedFiles(): List<DownloadedFileInfo> =
        withContext(Dispatchers.Default) {
            fileStorage.listFiles()
        }

    override suspend fun deleteFile(name: String): Boolean =
        withContext(Dispatchers.Default) {
            fileStorage.deleteFile(name)
        }

    override suspend fun deleteAllFiles(): Int =
        withContext(Dispatchers.Default) {
            fileStorage.deleteAllFiles()
        }

    override suspend fun downloadAndSave(
        url: String,
        filename: String,
    ): Boolean =
        try {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                val bytes = response.readRawBytes()
                withContext(Dispatchers.Default) {
                    fileStorage.saveFile(bytes, filename)
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }

    override suspend fun fileExists(name: String): Boolean =
        withContext(Dispatchers.Default) {
            fileStorage.fileExists(name)
        }
}
