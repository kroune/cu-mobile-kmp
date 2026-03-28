package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.LongreadMaterialsResponse
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val logger = KotlinLogging.logger {}

private const val UrlJsonKey = "url"

/**
 * API service for content-related endpoints (longreads, materials, file links).
 */
internal class ContentApiService(
    private val httpClient: HttpClient,
) {
    /** Fetches all materials for a longread. */
    suspend fun fetchLongreadMaterials(
        cookie: String,
        longreadId: String,
    ): List<LongreadMaterial>? =
        safeApiCall<LongreadMaterialsResponse>(
            logger,
            "fetch longread materials for longreadId=$longreadId",
        ) {
            val url = "${ApiEndpoints.Content.longreadMaterials(longreadId)}?limit=$MaxListLimit"
            httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
        }?.items

    /** Fetches a single material by ID. */
    suspend fun fetchMaterial(
        cookie: String,
        materialId: String,
    ): LongreadMaterial? =
        safeApiCall(logger, "fetch material materialId=$materialId") {
            httpClient.get(ApiEndpoints.Content.material(materialId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** @return the pre-signed download URL, or null. */
    suspend fun getDownloadLink(
        cookie: String,
        filename: String,
        version: String,
    ): String? =
        runCatchingCancellable {
            val url = ApiEndpoints.Content.DOWNLOAD_LINK +
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
        }.getOrElse { e ->
            logger.error(e) { "Failed to get download link for filename=$filename" }
            null
        }

    /** @return [UploadLinkData] with pre-signed upload URL, or null. */
    suspend fun getUploadLink(
        cookie: String,
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData? =
        safeApiCall(logger, "get upload link for filename=$filename") {
            val url = ApiEndpoints.Content.UPLOAD_LINK +
                "?directory=${directory.encodeUrlParam()}" +
                "&filename=${filename.encodeUrlParam()}" +
                "&contentType=${contentType.encodeUrlParam()}"
            httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /**
     * PUT file bytes to a pre-signed upload URL.
     *
     * The [presignedUrl] is an absolute URL (e.g. S3/MinIO), not relative to [BaseUrl].
     * No auth cookie is needed — the URL itself contains the authorization token.
     *
     * @return true if the upload succeeded (HTTP 2xx).
     */
    suspend fun uploadFileToUrl(
        presignedUrl: String,
        bytes: ByteArray,
        contentType: String,
    ): Boolean =
        runCatchingCancellable {
            val response = httpClient.put(presignedUrl) {
                setBody(ByteArrayContent(bytes, ContentType.parse(contentType)))
            }
            if (isSuccessStatus(response.status)) {
                true
            } else {
                logger.warn { "Upload to presigned URL returned ${response.status}" }
                false
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to upload file to presigned URL" }
            false
        }
}
