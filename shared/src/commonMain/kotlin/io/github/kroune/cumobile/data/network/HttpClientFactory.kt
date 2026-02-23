package io.github.kroune.cumobile.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates a configured [HttpClient] for communicating with the CU LMS API.
 *
 * Cookie attachment is handled separately by [ApiService] using the stored auth cookie.
 */
fun createHttpClient(): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                },
            )
        }
        defaultRequest {
            url(BaseUrl)
        }
    }

/** Base URL for the CU LMS API. */
internal const val BaseUrl = "https://my.centraluniversity.ru/api/"
