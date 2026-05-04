package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.ContentApiService
import io.github.kroune.cumobile.domain.model.LongreadMaterialDomain
import io.github.kroune.cumobile.domain.model.MaterialAttachmentDomain
import io.github.kroune.cumobile.domain.model.UploadLinkDataDomain
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Implementation of [ContentRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ContentApiService].
 */
internal class ContentRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val contentApi: Lazy<ContentApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    ContentRepository {
    override suspend fun fetchLongreadMaterials(longreadId: String): List<LongreadMaterialDomain>? =
        withCookie { cookie ->
            contentApi().fetchLongreadMaterials(cookie, longreadId)?.map { it.toDomain() }
        }

    override suspend fun fetchMaterial(materialId: String): LongreadMaterialDomain? =
        withCookie {
            contentApi().fetchMaterial(it, materialId)?.toDomain()
        }

    override suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String? =
        withCookie {
            contentApi().getDownloadLink(it, filename, version)
        }

    override suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkDataDomain? =
        withCookie {
            contentApi().getUploadLink(it, directory, filename, contentType)?.toDomain()
        }

    override suspend fun uploadFile(
        directory: String,
        filename: String,
        contentType: String,
        bytes: ByteArray,
    ): MaterialAttachmentDomain? {
        val uploadData = withCookie {
            contentApi().getUploadLink(it, directory, filename, contentType)?.toDomain()
        }
        if (uploadData == null) {
            logger.warn { "Failed to get upload link for $filename" }
            return null
        }
        val uploaded = contentApi().uploadFileToUrl(uploadData.url, bytes, contentType)
        if (!uploaded) {
            logger.warn { "Failed to upload $filename to presigned URL" }
            return null
        }
        return MaterialAttachmentDomain(
            name = uploadData.shortName.ifBlank { filename },
            filename = uploadData.filename,
            mediaType = contentType,
            length = bytes.size,
            version = uploadData.version,
        )
    }
}
