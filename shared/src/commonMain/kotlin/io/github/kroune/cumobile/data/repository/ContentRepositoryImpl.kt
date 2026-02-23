package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.ContentRepository

/**
 * Implementation of [ContentRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class ContentRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    ContentRepository {
    override suspend fun fetchLongreadMaterials(longreadId: Int): List<LongreadMaterial>? =
        withCookie {
            apiService.fetchLongreadMaterials(it, longreadId)
        }

    override suspend fun fetchMaterial(materialId: Int): LongreadMaterial? =
        withCookie {
            apiService.fetchMaterial(it, materialId)
        }

    override suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String? =
        withCookie {
            apiService.getDownloadLink(it, filename, version)
        }

    override suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? =
        withCookie {
            apiService.getUploadLink(it, directory, filename, contentType)
        }
}
