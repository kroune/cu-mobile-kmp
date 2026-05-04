package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.domain.model.LmsProfileDomain
import io.github.kroune.cumobile.domain.model.ProfileDomain
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke

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
    override suspend fun fetchProfile(): ProfileDomain? =
        withCookie { profileApi().fetchProfile(it) }?.toDomain()

    override suspend fun fetchLmsProfile(): LmsProfileDomain? =
        withCookie { profileApi().fetchLmsProfile(it) }?.toDomain()

    override suspend fun fetchAvatar(): ByteArray? =
        withCookie { profileApi().fetchAvatar(it) }

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
