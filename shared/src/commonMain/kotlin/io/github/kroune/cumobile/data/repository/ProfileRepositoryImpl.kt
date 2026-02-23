package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.ProfileRepository

/**
 * Implementation of [ProfileRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class ProfileRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    ProfileRepository {
    override suspend fun fetchProfile(): StudentProfile? =
        withCookie {
            apiService.fetchProfile(it)
        }

    override suspend fun fetchLmsProfile(): StudentLmsProfile? =
        withCookie {
            apiService.fetchLmsProfile(it)
        }

    override suspend fun fetchAvatar(): ByteArray? =
        withCookie {
            apiService.fetchAvatar(it)
        }

    override suspend fun deleteAvatar(): Boolean =
        withCookieOrFalse {
            apiService.deleteAvatar(it)
        }
}
