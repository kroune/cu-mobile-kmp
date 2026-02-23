package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * API service for task-related endpoints.
 */
internal class TaskApiService(
    private val httpClient: HttpClient,
) {
    /**
     * GET /micro-lms/tasks/student?state=…&state=…
     *
     * @param states list of task state filter values, e.g. `["inProgress", "backlog"]`.
     */
    suspend fun fetchTasks(
        cookie: String,
        states: List<String>,
    ): List<StudentTask>? =
        safeApiCall(logger, "fetch tasks") {
            val queryString = states.joinToString("&") { "state=$it" }
            httpClient.get("micro-lms/tasks/student?$queryString") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/tasks/{taskId} → [TaskDetails] */
    suspend fun fetchTaskDetails(
        cookie: String,
        taskId: Int,
    ): TaskDetails? =
        safeApiCall(logger, "fetch task details for taskId=$taskId") {
            httpClient.get("micro-lms/tasks/$taskId") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/tasks/{taskId}/events → list of [TaskEvent] */
    suspend fun fetchTaskEvents(
        cookie: String,
        taskId: Int,
    ): List<TaskEvent>? =
        safeApiCall(logger, "fetch task events for taskId=$taskId") {
            httpClient.get("micro-lms/tasks/$taskId/events") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/tasks/{taskId}/comments → list of [TaskComment] */
    suspend fun fetchTaskComments(
        cookie: String,
        taskId: Int,
    ): List<TaskComment>? =
        safeApiCall(logger, "fetch task comments for taskId=$taskId") {
            httpClient.get("micro-lms/tasks/$taskId/comments") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** PUT /micro-lms/tasks/{taskId}/start */
    suspend fun startTask(
        cookie: String,
        taskId: Int,
    ): Boolean =
        safeApiAction(logger, "start task taskId=$taskId") {
            httpClient.put("micro-lms/tasks/$taskId/start") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
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
        safeApiAction(logger, "submit task taskId=$taskId") {
            httpClient.put("micro-lms/tasks/$taskId/submit") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(
                    buildMap<String, Any> {
                        if (!solutionUrl.isNullOrBlank()) put("solutionUrl", solutionUrl)
                        put("attachments", attachments)
                    },
                )
            }
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
        safeApiAction(logger, "prolong late days for taskId=$taskId") {
            httpClient.put("micro-lms/tasks/$taskId/late-days-prolong") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(mapOf("lateDays" to lateDays))
            }
        }

    /** PUT /micro-lms/tasks/{taskId}/late-days-cancel */
    suspend fun cancelLateDays(
        cookie: String,
        taskId: Int,
    ): Boolean =
        safeApiAction(logger, "cancel late days for taskId=$taskId") {
            httpClient.put("micro-lms/tasks/$taskId/late-days-cancel") {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
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
                        "type" to CommentEntityType,
                        "content" to content,
                        "attachments" to attachments,
                    ),
                )
            }
            if (isSuccessStatus(response.status)) {
                val json: JsonElement = response.body()
                json.jsonObject["commentId"]
                    ?.jsonPrimitive
                    ?.content
                    ?.toIntOrNull()
            } else {
                logger.warn { "create comment for taskId=$taskId returned ${response.status}" }
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to create comment for taskId=$taskId" }
            null
        }
}
