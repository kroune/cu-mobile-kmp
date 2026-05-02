package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.NotificationApiService
import io.github.kroune.cumobile.domain.model.NotificationDomain
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke

/**
 * Implementation of [NotificationRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [NotificationApiService].
 */
internal class NotificationRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val notificationApi: Lazy<NotificationApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    NotificationRepository {
    override suspend fun fetchNotifications(
        category: Int,
        limit: Int,
        offset: Int,
    ): List<NotificationDomain>? =
        withCookie { notificationApi().fetchNotifications(it, category, limit, offset) }
            ?.map { it.toDomain() }
}
