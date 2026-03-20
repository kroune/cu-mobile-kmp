package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [FileRepository].
 *
 * Uses [FileStorage] for local disk operations and [HttpClient]
 * for downloading files from pre-signed URLs.
 */
internal class FileRepositoryImpl(
    private val fileStorage: FileStorage,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FileRepository {
    override suspend fun listDownloadedFiles(): List<DownloadedFileInfo> =
        withContext(dispatcher) {
            fileStorage.listFiles()
        }

    override suspend fun deleteFile(name: String): Boolean =
        withContext(dispatcher) {
            fileStorage.deleteFile(name)
        }

    override suspend fun deleteAllFiles(): Int =
        withContext(dispatcher) {
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
                withContext(dispatcher) {
                    fileStorage.saveFile(bytes, filename)
                }
            } else {
                logger.warn { "downloadAndSave: unexpected status ${response.status}" }
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to download and save file: $filename" }
            false
        }

    override suspend fun fileExists(name: String): Boolean =
        withContext(dispatcher) {
            fileStorage.fileExists(name)
        }
}
