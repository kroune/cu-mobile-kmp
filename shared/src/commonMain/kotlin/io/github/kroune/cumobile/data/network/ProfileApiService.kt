package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

private val logger = KotlinLogging.logger {}

/**
 * API service for user profile endpoints.
 */
internal class ProfileApiService(
    private val httpClient: HttpClient,
) {
    /**
     * Checks whether the given cookie is accepted by the server.
     *
     * Returns `true` if the server responds with a success status,
     * `false` if it responds with 401 (Unauthorized),
     * or `null` if the request failed due to a network error.
     */
    suspend fun checkAuth(cookie: String): Boolean? =
        runCatchingCancellable {
            val response = httpClient.get(ApiEndpoints.Profile.ME) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status.isSuccess()) {
                true
            } else if (response.status == HttpStatusCode.Unauthorized) {
                false
            } else {
                logger.warn { "checkAuth: unexpected status ${response.status}" }
                null
            }
        }.getOrElse { e ->
            logger.error(e) { "checkAuth: network error" }
            null
        }

    /** Fetches the current student's hub profile. */
    suspend fun fetchProfile(cookie: String): StudentProfile? =
        safeApiCall(logger, "fetch profile") {
            httpClient.get(ApiEndpoints.Profile.ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the current student's avatar as raw image bytes. */
    suspend fun fetchAvatar(cookie: String): ByteArray? =
        runCatchingCancellable {
            val response = httpClient.get(ApiEndpoints.Profile.AVATAR_ME) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) {
                response.readRawBytes()
            } else {
                logger.warn { "fetch avatar returned ${response.status}" }
                null
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to fetch avatar" }
            null
        }

    /** Uploads a new avatar image as multipart. */
    suspend fun uploadAvatar(
        cookie: String,
        bytes: ByteArray,
        contentType: String,
    ): Boolean =
        safeApiAction(logger, "upload avatar") {
            httpClient.submitFormWithBinaryData(
                url = ApiEndpoints.Profile.AVATAR_ME,
                formData = formData {
                    append(
                        "file",
                        bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, contentType)
                            append(HttpHeaders.ContentDisposition, "filename=\"avatar\"")
                        },
                    )
                },
            ) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Deletes the current student's avatar. */
    suspend fun deleteAvatar(cookie: String): Boolean =
        safeApiAction(logger, "delete avatar") {
            httpClient.delete(ApiEndpoints.Profile.AVATAR_ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the current student's LMS profile. */
    suspend fun fetchLmsProfile(cookie: String): StudentLmsProfile? =
        safeApiCall(logger, "fetch LMS profile") {
            httpClient.get(ApiEndpoints.Profile.LMS_ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
