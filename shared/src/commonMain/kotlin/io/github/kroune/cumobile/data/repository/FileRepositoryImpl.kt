package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [FileRepository].
 *
 * Uses [FileStorage] for local disk operations and [HttpClient]
 * for downloading files from pre-signed URLs.
 */
internal class FileRepositoryImpl(
    fileStorage: Lazy<FileStorage>,
    httpClient: Lazy<HttpClient>,
    private val dispatchers: Lazy<AppDispatchers>,
) : FileRepository {
    private val fileStorage by fileStorage
    private val httpClient by httpClient

    override suspend fun listDownloadedFiles(): List<DownloadedFileInfo> =
        withContext(dispatchers().io) {
            fileStorage.listFiles()
        }

    override suspend fun deleteFile(name: String): Boolean =
        withContext(dispatchers().io) {
            fileStorage.deleteFile(name)
        }

    override suspend fun deleteAllFiles(): Int =
        withContext(dispatchers().io) {
            fileStorage.deleteAllFiles()
        }

    override suspend fun downloadAndSave(
        url: String,
        filename: String,
    ): Boolean =
        runCatchingCancellable {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                val bytes = response.readRawBytes()
                withContext(dispatchers().io) {
                    fileStorage.saveFile(bytes, filename)
                }
            } else {
                logger.warn { "downloadAndSave: unexpected status ${response.status}" }
                false
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to download and save file: $filename" }
            false
        }

    override suspend fun fileExists(name: String): Boolean =
        withContext(dispatchers().io) {
            fileStorage.fileExists(name)
        }
}
