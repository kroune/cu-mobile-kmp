package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * In-app notification item.
 */
@Serializable
data class NotificationApi(
    val id: String = "",
    val notificationId: String = "",
    /** ISO 8601 datetime string. */
    val createdAt: String = "",
    val category: String = "",
    val groupingKey: String = "",
    val icon: String = "",
    val title: String = "",
    val description: String = "",
    val link: NotificationLinkApi? = null,
    /** ISO 8601 datetime string. */
    val startDate: String? = null,
    /** ISO 8601 datetime string. */
    val endDate: String? = null,
    val previewImageUri: String? = null,
)

/** Deep-link associated with a [NotificationApi]. */
@Serializable
data class NotificationLinkApi(
    val uri: String = "",
    val label: String = "",
    val target: String = "",
)

/**
 * Request body for fetching notifications.
 */
@Serializable
data class NotificationRequestApi(
    val paging: NotificationPagingApi,
    val filter: NotificationFilterApi,
) {
    companion object {
        /** Convenience factory for creating a standard notification request. */
        fun create(
            category: Int,
            limit: Int = 100,
            offset: Int = 0,
        ): NotificationRequestApi =
            NotificationRequestApi(
                paging = NotificationPagingApi(limit = limit, offset = offset),
                filter = NotificationFilterApi(category),
            )
    }
}

/** Paging parameters for [NotificationRequestApi]. */
@Serializable
data class NotificationPagingApi(
    val limit: Int,
    val offset: Int,
    val sorting: List<String> = emptyList(),
)

/** Filter parameters for [NotificationRequestApi]. category is an integer ID. */
@Serializable
data class NotificationFilterApi(
    val category: Int,
)

/**
 * Named constants for notification category IDs used in the API.
 *
 * Category `1` = education-related, category `2` = everything else.
 */
object NotificationCategoryApi {
    const val Education = 1
    const val Other = 2
}
