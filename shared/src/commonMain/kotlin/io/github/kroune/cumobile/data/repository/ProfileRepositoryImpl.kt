package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers

/**
 * Implementation of [ProfileRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ProfileApiService].
 */
internal class ProfileRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val profileApi: Lazy<ProfileApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    ProfileRepository {
    override suspend fun fetchProfile(): StudentProfile? =
        withCookie { profileApi().fetchProfile(it) }

    override suspend fun fetchLmsProfile(): StudentLmsProfile? =
        withCookie { profileApi().fetchLmsProfile(it) }

    override suspend fun fetchAvatar(): ByteArray =
        withCookie { profileApi().fetchAvatar(it) }
            ?: error("Cannot fetch avatar: no auth cookie")

    override suspend fun uploadAvatar(
        bytes: ByteArray,
        contentType: String,
    ): Boolean =
        withCookieOrFalse {
            profileApi().uploadAvatar(it, bytes, contentType)
        }

    override suspend fun deleteAvatar(): Boolean =
        withCookieOrFalse { profileApi().deleteAvatar(it) }
}
