package io.github.kroune.cumobile.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val RequestTimeoutMs = 30_000L
private const val ConnectTimeoutMs = 15_000L
private const val SocketTimeoutMs = 30_000L

/**
 * Creates a configured [HttpClient] for communicating with the CU LMS API.
 *
 * Cookie attachment is handled separately by individual API services
 * using the stored auth cookie.
 */
internal fun createHttpClient(): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    // lenient parsing is required because the CU LMS API
                    // occasionally returns unquoted or single-quoted JSON values
                    isLenient = true
                    encodeDefaults = true
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

/** Base domain for the CU LMS service. */
internal const val BaseDomain = "https://my.centraluniversity.ru"

/** Base URL for the CU LMS API. */
internal const val BaseUrl = "$BaseDomain/api/"
