package io.github.kroune.cumobile.data.network

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLQueryComponent
import kotlin.coroutines.cancellation.CancellationException

/** Cookie name used for authentication with the CU LMS BFF. */
internal const val TargetCookieName = "bff.cookie"

/** Default list limit for paginated API endpoints. */
internal const val MaxListLimit = 10_000

/** Max chars to read from an error response body for logging. */
private const val ErrorBodyMaxChars = 500

/** Entity type identifier used in the comments API. */
internal const val CommentEntityType = "task"

/** Formats a cookie string for the HTTP Cookie header. */
internal fun cookieHeader(cookie: String): String =
    "$TargetCookieName=$cookie"

/** Checks whether the HTTP status indicates a successful mutation (200, 201, 204). */
internal fun isSuccessStatus(status: HttpStatusCode): Boolean =
    status == HttpStatusCode.OK ||
        status == HttpStatusCode.Created ||
        status == HttpStatusCode.NoContent

/**
 * Executes an API call and deserializes the response body on HTTP 200.
 *
 * - Returns `null` on non-200 responses (with a warning log).
 * - Rethrows [CancellationException] to preserve structured concurrency.
 * - Catches and logs all other exceptions, returning `null`.
 */
internal suspend inline fun <reified T> safeApiCall(
    logger: KLogger,
    description: String,
    crossinline block: suspend () -> HttpResponse,
): T? =
    try {
        val response = block()
        if (response.status == HttpStatusCode.OK) {
            response.body<T>()
        } else {
            val body = try {
                response.bodyAsText().take(ErrorBodyMaxChars)
            } catch (ignored: Exception) {
                "<unable to read body>"
            }
            logger.warn { "$description returned ${response.status}, body: $body" }
            null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.error(e) { "Failed to $description" }
        null
    }

/**
 * Executes a mutating API call and returns whether it succeeded.
 *
 * - Returns `false` on non-success responses (with a warning log).
 * - Rethrows [CancellationException] to preserve structured concurrency.
 * - Catches and logs all other exceptions, returning `false`.
 */
internal suspend inline fun safeApiAction(
    logger: KLogger,
    description: String,
    crossinline block: suspend () -> HttpResponse,
): Boolean =
    try {
        val response = block()
        if (isSuccessStatus(response.status)) {
            true
        } else {
            logger.warn { "$description returned ${response.status}" }
            false
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.error(e) { "Failed to $description" }
        false
    }

/** URL-encodes a string for use in query parameters. */
internal fun String.encodeUrlParam(): String =
    encodeURLQueryComponent()
