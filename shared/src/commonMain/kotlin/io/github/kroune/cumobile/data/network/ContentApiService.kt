package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

private const val UrlJsonKey = "url"

/**
 * API service for content-related endpoints (longreads, materials, file links).
 */
internal class ContentApiService(
    private val httpClient: HttpClient,
) {
    /** GET /micro-lms/longreads/{longreadId}/materials?limit=10000 */
    suspend fun fetchLongreadMaterials(
        cookie: String,
        longreadId: Int,
    ): List<LongreadMaterial>? =
        safeApiCall(logger, "fetch longread materials for longreadId=$longreadId") {
            val url = "micro-lms/longreads/$longreadId/materials?limit=$MaxListLimit"
            httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/materials/{materialId} → [LongreadMaterial] */
    suspend fun fetchMaterial(
        cookie: String,
        materialId: Int,
    ): LongreadMaterial? =
        safeApiCall(logger, "fetch material materialId=$materialId") {
            httpClient.get("micro-lms/materials/$materialId") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /**
     * GET /micro-lms/content/download-link?filename=…&version=…
     * @return the pre-signed download URL, or null.
     */
    suspend fun getDownloadLink(
        cookie: String,
        filename: String,
        version: String,
    ): String? =
        try {
            val url = "micro-lms/content/download-link" +
                "?filename=${filename.encodeUrlParam()}&version=$version"
            val response = httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) {
                val json: JsonElement = response.body()
                json.jsonObject[UrlJsonKey]?.jsonPrimitive?.content
            } else {
                logger.warn { "get download link for filename=$filename returned ${response.status}" }
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to get download link for filename=$filename" }
            null
        }

    /**
     * GET /micro-lms/content/upload-link?directory=…&filename=…&contentType=…
     * @return [UploadLinkData] with pre-signed upload URL, or null.
     */
    suspend fun getUploadLink(
        cookie: String,
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? =
        safeApiCall(logger, "get upload link for filename=$filename") {
            val url = "micro-lms/content/upload-link" +
                "?directory=${directory.encodeUrlParam()}" +
                "&filename=${filename.encodeUrlParam()}" +
                "&contentType=${contentType.encodeUrlParam()}"
            httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
