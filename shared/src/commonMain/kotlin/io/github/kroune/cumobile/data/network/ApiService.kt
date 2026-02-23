package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.model.NotificationRequest
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.data.model.UploadLinkData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Centralized API service for all CU LMS network calls.
 *
 * Every authenticated method accepts a [cookie] parameter — the raw
 * `bff.cookie` value retrieved from [AuthLocalDataSource].
 * Methods return `null` / `false` / empty list on failure, never throw.
 */
class ApiService(
    private val httpClient: HttpClient,
) {
    // ── helpers ────────────────────────────────────────────────────────

    private fun cookieHeader(cookie: String): String = "bff.cookie=$cookie"

    private fun isSuccess(status: HttpStatusCode): Boolean =
        status == HttpStatusCode.OK ||
            status == HttpStatusCode.Created ||
            status == HttpStatusCode.NoContent

    // ── auth ───────────────────────────────────────────────────────────

    /**
     * Validates the current auth cookie by calling the profile endpoint.
     * @return true if the cookie is valid (200 response).
     */
    suspend fun validateAuth(cookie: String): Boolean =
        try {
            val response = httpClient.get("hub/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }

    // ── profile ────────────────────────────────────────────────────────

    /** GET /hub/students/me → [StudentProfile] */
    suspend fun fetchProfile(cookie: String): StudentProfile? =
        try {
            val response = httpClient.get("hub/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /hub/students/me → raw JSON string (for validation). */
    suspend fun fetchProfileRaw(cookie: String): String? =
        try {
            val response = httpClient.get("hub/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (_: Exception) {
            null
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
                null
            }
        } catch (_: Exception) {
            null
        }

    /** DELETE /hub/avatars/me */
    suspend fun deleteAvatar(cookie: String): Boolean =
        try {
            val response = httpClient.delete("hub/avatars/me") {
                header("Cookie", cookieHeader(cookie))
            }
            isSuccess(response.status)
        } catch (_: Exception) {
            false
        }

    /** GET /micro-lms/students/me → [StudentLmsProfile] */
    suspend fun fetchLmsProfile(cookie: String): StudentLmsProfile? =
        try {
            val response = httpClient.get("micro-lms/students/me") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    // ── tasks ──────────────────────────────────────────────────────────

    /**
     * GET /micro-lms/tasks/student?state=…&state=…
     *
     * @param states list of task state filter values, e.g. `["inProgress", "backlog"]`.
     *   Known values: "inProgress", "review", "backlog", "failed", "evaluated".
     */
    suspend fun fetchTasks(
        cookie: String,
        states: List<String>,
    ): List<StudentTask>? =
        try {
            val queryString = states.joinToString("&") { "state=$it" }
            val response = httpClient.get("micro-lms/tasks/student?$queryString") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/tasks/{taskId} → [TaskDetails] */
    suspend fun fetchTaskDetails(
        cookie: String,
        taskId: Int,
    ): TaskDetails? =
        try {
            val response = httpClient.get("micro-lms/tasks/$taskId") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/tasks/{taskId}/events → list of [TaskEvent] */
    suspend fun fetchTaskEvents(
        cookie: String,
        taskId: Int,
    ): List<TaskEvent>? =
        try {
            val response = httpClient.get("micro-lms/tasks/$taskId/events") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/tasks/{taskId}/comments → list of [TaskComment] */
    suspend fun fetchTaskComments(
        cookie: String,
        taskId: Int,
    ): List<TaskComment>? =
        try {
            val response = httpClient.get("micro-lms/tasks/$taskId/comments") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** PUT /micro-lms/tasks/{taskId}/start */
    suspend fun startTask(
        cookie: String,
        taskId: Int,
    ): Boolean =
        try {
            val response = httpClient.put("micro-lms/tasks/$taskId/start") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
            isSuccess(response.status)
        } catch (_: Exception) {
            false
        }

    /**
     * PUT /micro-lms/tasks/{taskId}/submit
     *
     * @param solutionUrl optional URL of the solution.
     * @param attachments list of attachment metadata maps.
     */
    suspend fun submitTask(
        cookie: String,
        taskId: Int,
        solutionUrl: String? = null,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Boolean =
        try {
            val response = httpClient.put("micro-lms/tasks/$taskId/submit") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(
                    buildMap<String, Any> {
                        if (!solutionUrl.isNullOrBlank()) put("solutionUrl", solutionUrl)
                        put("attachments", attachments)
                    },
                )
            }
            isSuccess(response.status)
        } catch (_: Exception) {
            false
        }

    /**
     * PUT /micro-lms/tasks/{taskId}/late-days-prolong
     *
     * @param lateDays number of late days to request.
     */
    suspend fun prolongLateDays(
        cookie: String,
        taskId: Int,
        lateDays: Int,
    ): Boolean =
        try {
            val response = httpClient.put(
                "micro-lms/tasks/$taskId/late-days-prolong",
            ) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(mapOf("lateDays" to lateDays))
            }
            isSuccess(response.status)
        } catch (_: Exception) {
            false
        }

    /** PUT /micro-lms/tasks/{taskId}/late-days-cancel */
    suspend fun cancelLateDays(
        cookie: String,
        taskId: Int,
    ): Boolean =
        try {
            val response = httpClient.put(
                "micro-lms/tasks/$taskId/late-days-cancel",
            ) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
            isSuccess(response.status)
        } catch (_: Exception) {
            false
        }

    /**
     * POST /micro-lms/comments
     *
     * @return the created comment's ID, or null on failure.
     */
    suspend fun createComment(
        cookie: String,
        taskId: Int,
        content: String,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Int? =
        try {
            val response = httpClient.post("micro-lms/comments") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "entityId" to taskId,
                        "type" to "task",
                        "content" to content,
                        "attachments" to attachments,
                    ),
                )
            }
            if (isSuccess(response.status)) {
                val json: JsonElement = response.body()
                json.jsonObject["commentId"]
                    ?.jsonPrimitive
                    ?.content
                    ?.toIntOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

    // ── courses ────────────────────────────────────────────────────────

    /** GET /micro-lms/courses/student?limit=10000 → list of [Course] */
    suspend fun fetchCourses(cookie: String): List<Course>? =
        try {
            val response = httpClient.get("micro-lms/courses/student?limit=10000") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/courses/{courseId}/overview → [CourseOverview] */
    suspend fun fetchCourseOverview(
        cookie: String,
        courseId: Int,
    ): CourseOverview? =
        try {
            val response = httpClient.get("micro-lms/courses/$courseId/overview") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    // ── content ────────────────────────────────────────────────────────

    /** GET /micro-lms/longreads/{longreadId}/materials?limit=10000 */
    suspend fun fetchLongreadMaterials(
        cookie: String,
        longreadId: Int,
    ): List<LongreadMaterial>? =
        try {
            val url = "micro-lms/longreads/$longreadId/materials?limit=10000"
            val response = httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/materials/{materialId} → [LongreadMaterial] */
    suspend fun fetchMaterial(
        cookie: String,
        materialId: Int,
    ): LongreadMaterial? =
        try {
            val response = httpClient.get("micro-lms/materials/$materialId") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
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
                json.jsonObject["url"]?.jsonPrimitive?.content
            } else {
                null
            }
        } catch (_: Exception) {
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
        try {
            val url = "micro-lms/content/upload-link" +
                "?directory=${directory.encodeUrlParam()}" +
                "&filename=${filename.encodeUrlParam()}" +
                "&contentType=${contentType.encodeUrlParam()}"
            val response = httpClient.get(url) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    // ── notifications ──────────────────────────────────────────────────

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
        try {
            val body = NotificationRequest.create(
                category = category,
                limit = limit,
                offset = offset,
            )
            val response = httpClient.post(
                "notification-hub/notifications/in-app",
            ) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<NotificationItem>>()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

    // ── performance ────────────────────────────────────────────────────

    /** GET /micro-lms/performance/student */
    suspend fun fetchPerformance(cookie: String): StudentPerformanceResponse? =
        try {
            val response = httpClient.get("micro-lms/performance/student") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/courses/{courseId}/exercises */
    suspend fun fetchCourseExercises(
        cookie: String,
        courseId: Int,
    ): CourseExercisesResponse? =
        try {
            val response = httpClient.get(
                "micro-lms/courses/$courseId/exercises",
            ) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/courses/{courseId}/student-performance */
    suspend fun fetchCoursePerformance(
        cookie: String,
        courseId: Int,
    ): CourseStudentPerformanceResponse? =
        try {
            val response = httpClient.get(
                "micro-lms/courses/$courseId/student-performance",
            ) {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }

    /** GET /micro-lms/gradebook */
    suspend fun fetchGradebook(cookie: String): GradebookResponse? =
        try {
            val response = httpClient.get("micro-lms/gradebook") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (_: Exception) {
            null
        }
}

/** URL-encodes a string for use in query parameters. */
private fun String.encodeUrlParam(): String = encodeURLQueryComponent()
