package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [ProfileRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class ProfileRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : ProfileRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchProfile(): StudentProfile? {
        val c = cookie() ?: return null
        return apiService.fetchProfile(c)
    }

    override suspend fun fetchLmsProfile(): StudentLmsProfile? {
        val c = cookie() ?: return null
        return apiService.fetchLmsProfile(c)
    }

    override suspend fun fetchAvatar(): ByteArray? {
        val c = cookie() ?: return null
        return apiService.fetchAvatar(c)
    }

    override suspend fun deleteAvatar(): Boolean {
        val c = cookie() ?: return false
        return apiService.deleteAvatar(c)
    }
}
