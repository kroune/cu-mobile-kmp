package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * API service for user profile endpoints.
 */
internal class ProfileApiService(
    private val httpClient: HttpClient,
) {
    /** GET /hub/students/me → [StudentProfile] */
    suspend fun fetchProfile(cookie: String): StudentProfile? =
        safeApiCall(logger, "fetch profile") {
            httpClient.get("hub/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /hub/avatars/me → avatar image bytes. */
    suspend fun fetchAvatar(cookie: String): ByteArray? =
        try {
            val response = httpClient.get("hub/avatars/me") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) {
                response.readRawBytes()
            } else {
                logger.warn { "fetch avatar returned ${response.status}" }
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch avatar" }
            null
        }

    /** DELETE /hub/avatars/me */
    suspend fun deleteAvatar(cookie: String): Boolean =
        safeApiAction(logger, "delete avatar") {
            httpClient.delete("hub/avatars/me") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/students/me → [StudentLmsProfile] */
    suspend fun fetchLmsProfile(cookie: String): StudentLmsProfile? =
        safeApiCall(logger, "fetch LMS profile") {
            httpClient.get("micro-lms/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
