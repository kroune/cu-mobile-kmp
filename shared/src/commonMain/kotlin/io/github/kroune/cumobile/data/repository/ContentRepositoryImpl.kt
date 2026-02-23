package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.ContentRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [ContentRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class ContentRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : ContentRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchLongreadMaterials(longreadId: Int): List<LongreadMaterial>? {
        val c = cookie() ?: return null
        return apiService.fetchLongreadMaterials(c, longreadId)
    }

    override suspend fun fetchMaterial(materialId: Int): LongreadMaterial? {
        val c = cookie() ?: return null
        return apiService.fetchMaterial(c, materialId)
    }

    override suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String? {
        val c = cookie() ?: return null
        return apiService.getDownloadLink(c, filename, version)
    }

    override suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? {
        val c = cookie() ?: return null
        return apiService.getUploadLink(c, directory, filename, contentType)
    }
}
