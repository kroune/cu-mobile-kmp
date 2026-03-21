package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.kroune.cumobile.data.network.ContentApiService
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Implementation of [ContentRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ContentApiService].
 */
internal class ContentRepositoryImpl(
    authLocal: AuthLocalDataSource,
    private val contentApi: ContentApiService,
) : CookieAwareRepository(authLocal),
    ContentRepository {
    override suspend fun fetchLongreadMaterials(longreadId: Int): List<LongreadMaterial>? =
        withCookie { contentApi.fetchLongreadMaterials(it, longreadId) }

    override suspend fun fetchMaterial(materialId: Int): LongreadMaterial? =
        withCookie { contentApi.fetchMaterial(it, materialId) }

    override suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String? =
        withCookie { contentApi.getDownloadLink(it, filename, version) }

    override suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? =
        withCookie { contentApi.getUploadLink(it, directory, filename, contentType) }

    override suspend fun uploadFile(
        directory: String,
        filename: String,
        contentType: String,
        bytes: ByteArray,
    ): MaterialAttachment? {
        val uploadData = getUploadLink(directory, filename, contentType)
        if (uploadData == null) {
            logger.warn { "Failed to get upload link for $filename" }
            return null
        }
        val uploaded = contentApi.uploadFileToUrl(uploadData.url, bytes, contentType)
        if (!uploaded) {
            logger.warn { "Failed to upload $filename to presigned URL" }
            return null
        }
        return MaterialAttachment(
            name = uploadData.shortName.ifBlank { filename },
            filename = uploadData.filename,
            mediaType = contentType,
            length = bytes.size,
            version = uploadData.version,
        )
    }
}
