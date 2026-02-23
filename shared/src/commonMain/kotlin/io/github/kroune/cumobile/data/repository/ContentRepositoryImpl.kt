@file:Suppress("MaxLineLength")

package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.kroune.cumobile.data.network.ContentApiService
import io.github.kroune.cumobile.domain.repository.ContentRepository

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

    override suspend fun fetchMaterial(materialId: Int): LongreadMaterial? = withCookie { contentApi.fetchMaterial(it, materialId) }

    override suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String? = withCookie { contentApi.getDownloadLink(it, filename, version) }

    override suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? = withCookie { contentApi.getUploadLink(it, directory, filename, contentType) }
}
