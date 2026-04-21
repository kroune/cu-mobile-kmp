package io.github.kroune.cumobile.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val RequestTimeoutMs = 30_000L
private const val ConnectTimeoutMs = 15_000L
private const val SocketTimeoutMs = 30_000L
private const val AuthTimeoutMs = 30_000L

/**
 * Creates a configured [HttpClient] for communicating with the CU LMS API.
 *
 * Cookie attachment is handled separately by individual API services
 * using the stored auth cookie.
 */
internal fun createHttpClient(): HttpClient =
    HttpClient {
        followRedirects = true
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    // lenient parsing is required because the CU LMS API
                    // occasionally returns unquoted or single-quoted JSON values
                    isLenient = true
                    encodeDefaults = true
                    // API may return null for fields with defaults (e.g. maxScore)
                    coerceInputValues = true
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = RequestTimeoutMs
            connectTimeoutMillis = ConnectTimeoutMs
            socketTimeoutMillis = SocketTimeoutMs
        }
        defaultRequest {
            url(BaseUrl)
        }
    }

/**
 * Creates a dedicated [HttpClient] for the Keycloak auth flow.
 *
 * Key differences from the API client:
 * - `followRedirects = false` — auth flow manually intercepts 302 redirects
 * - [HttpCookies] with [ResettableCookieStorage] — per-session cookie jar
 *   that can be cleared between login attempts
 * - No [ContentNegotiation] — auth pages are HTML, not JSON
 * - No `defaultRequest` base URL — hits multiple hosts (id.centraluniversity.ru, my.centraluniversity.ru)
 */
internal fun createAuthHttpClient(cookieStorage: ResettableCookieStorage): HttpClient =
    HttpClient {
        followRedirects = false
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(HttpTimeout) {
            requestTimeoutMillis = AuthTimeoutMs
            connectTimeoutMillis = AuthTimeoutMs
            socketTimeoutMillis = AuthTimeoutMs
        }
    }

/** Base domain for the CU LMS service. */
internal const val BaseDomain = "https://my.centraluniversity.ru"

/** Base URL for the CU LMS API. */
internal const val BaseUrl = "$BaseDomain/api/"
