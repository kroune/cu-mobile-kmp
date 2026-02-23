package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [NotificationRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class NotificationRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : NotificationRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchNotifications(
        category: Int,
        limit: Int,
        offset: Int,
    ): List<NotificationItem>? {
        val c = cookie() ?: return null
        return apiService.fetchNotifications(c, category, limit, offset)
    }
}
