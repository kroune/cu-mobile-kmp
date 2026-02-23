package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.NotificationRepository

/**
 * Implementation of [NotificationRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class NotificationRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    NotificationRepository {
    override suspend fun fetchNotifications(
        category: Int,
        limit: Int,
        offset: Int,
    ): List<NotificationItem>? =
        withCookie {
            apiService.fetchNotifications(it, category, limit, offset)
        }
}
