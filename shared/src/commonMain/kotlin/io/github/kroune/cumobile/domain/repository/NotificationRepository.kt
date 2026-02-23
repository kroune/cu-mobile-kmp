package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.NotificationItem

/** Repository for in-app notifications. */
interface NotificationRepository {
    /**
     * Fetches notifications for a given category.
     *
     * @param category notification category ID (e.g. 1 = education, 2 = other).
     * @param limit page size.
     * @param offset page offset.
     */
    suspend fun fetchNotifications(
        category: Int,
        limit: Int = 100,
        offset: Int = 0,
    ): List<NotificationItem>?
}
