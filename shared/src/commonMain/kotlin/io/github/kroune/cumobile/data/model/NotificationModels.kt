package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * In-app notification item.
 *
 * API endpoint: `POST /notification-hub/notifications/in-app`
 */
@Serializable
data class NotificationItem(
    val id: Int = 0,
    /** ISO 8601 datetime string. */
    val createdAt: String = "",
    val category: String = "",
    val icon: String = "",
    val title: String = "",
    val description: String = "",
    val link: NotificationLink? = null,
)

/** Deep-link associated with a [NotificationItem]. */
@Serializable
data class NotificationLink(
    val uri: String = "",
    val label: String = "",
    val target: String = "",
)

/**
 * Request body for fetching notifications.
 *
 * API endpoint: `POST /notification-hub/notifications/in-app`
 */
@Serializable
data class NotificationRequest(
    val paging: NotificationPaging,
    val filter: NotificationFilter,
) {
    companion object {
        /** Convenience factory for creating a standard notification request. */
        fun create(
            category: Int,
            limit: Int = 100,
            offset: Int = 0,
        ): NotificationRequest =
            NotificationRequest(
                paging = NotificationPaging(limit = limit, offset = offset),
                filter = NotificationFilter(category),
            )
    }
}

/** Paging parameters for [NotificationRequest]. */
@Serializable
data class NotificationPaging(
    val limit: Int,
    val offset: Int,
    val sorting: List<String> = emptyList(),
)

/** Filter parameters for [NotificationRequest]. category is an integer ID. */
@Serializable
data class NotificationFilter(
    val category: Int,
)

/**
 * Named constants for notification category IDs used in the API.
 *
 * Category `1` = education-related, category `2` = everything else.
 */
object NotificationCategory {
    const val Education = 1
    const val Other = 2
}
