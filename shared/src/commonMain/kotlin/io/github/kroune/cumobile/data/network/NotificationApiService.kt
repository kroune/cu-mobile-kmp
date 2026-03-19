package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.model.NotificationRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private val logger = KotlinLogging.logger {}

/**
 * API service for notification endpoints.
 */
internal class NotificationApiService(
    private val httpClient: HttpClient,
) {
    /**
     * POST /notification-hub/notifications/in-app
     *
     * @param category notification category filter (e.g. 1 = education, 2 = other).
     * @param limit page size.
     * @param offset page offset.
     */
    suspend fun fetchNotifications(
        cookie: String,
        category: Int,
        limit: Int = 100,
        offset: Int = 0,
    ): List<NotificationItem>? =
        safeApiCall(logger, "fetch notifications for category=$category") {
            val body = NotificationRequest.create(
                category = category,
                limit = limit,
                offset = offset,
            )
            httpClient.post(ApiEndpoints.NOTIFICATIONS_IN_APP) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
}
