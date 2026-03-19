package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
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
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * API service for user profile endpoints.
 */
internal class ProfileApiService(
    private val httpClient: HttpClient,
) {
    /** Fetches the current student's hub profile. */
    suspend fun fetchProfile(cookie: String): StudentProfile? =
        safeApiCall(logger, "fetch profile") {
            httpClient.get(ApiEndpoints.PROFILE_ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the current student's avatar as raw image bytes. */
    suspend fun fetchAvatar(cookie: String): ByteArray? =
        try {
            val response = httpClient.get(ApiEndpoints.AVATAR_ME) {
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

    /** Uploads a new avatar image as multipart. */
    suspend fun uploadAvatar(
        cookie: String,
        bytes: ByteArray,
        contentType: String,
    ): Boolean =
        safeApiAction(logger, "upload avatar") {
            httpClient.submitFormWithBinaryData(
                url = ApiEndpoints.AVATAR_ME,
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
            httpClient.delete(ApiEndpoints.AVATAR_ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the current student's LMS profile. */
    suspend fun fetchLmsProfile(cookie: String): StudentLmsProfile? =
        safeApiCall(logger, "fetch LMS profile") {
            httpClient.get(ApiEndpoints.LMS_PROFILE_ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
