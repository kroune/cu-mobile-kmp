package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.network.NotificationApiService
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers

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
    ): List<NotificationItem>? =
        withCookie { notificationApi().fetchNotifications(it, category, limit, offset) }
}
