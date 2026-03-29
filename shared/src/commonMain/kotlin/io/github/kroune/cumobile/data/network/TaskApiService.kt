package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StartTaskResponse
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.util.runCatchingCancellable
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

private val logger = KotlinLogging.logger {}

/**
 * API service for task-related endpoints.
 */
internal class TaskApiService(
    private val httpClient: HttpClient,
) {
    /** @param states list of task state filter values, e.g. `["inProgress", "backlog"]`. */
    suspend fun fetchTasks(
        cookie: String,
        states: List<String>,
    ): List<StudentTask>? =
        safeApiCall(logger, "fetch tasks") {
            val queryString = states.joinToString("&") { "state=$it" }
            httpClient.get("${ApiEndpoints.Tasks.STUDENT}?$queryString") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches task details by [taskId]. */
    suspend fun fetchTaskDetails(
        cookie: String,
        taskId: String,
    ): TaskDetails? =
        safeApiCall(logger, "fetch task details for taskId=$taskId") {
            httpClient.get(ApiEndpoints.Tasks.byId(taskId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the event history for a task. */
    suspend fun fetchTaskEvents(
        cookie: String,
        taskId: String,
    ): List<TaskEvent>? =
        safeApiCall(logger, "fetch task events for taskId=$taskId") {
            httpClient.get(ApiEndpoints.Tasks.events(taskId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches comments for a task. */
    suspend fun fetchTaskComments(
        cookie: String,
        taskId: String,
    ): List<TaskComment>? =
        safeApiCall(logger, "fetch task comments for taskId=$taskId") {
            httpClient.get(ApiEndpoints.Tasks.comments(taskId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Starts a backlog task. Returns the response containing quiz session ID if applicable. */
    suspend fun startTask(
        cookie: String,
        taskId: String,
    ): StartTaskResponse? =
        safeApiCall(logger, "start task taskId=$taskId") {
            httpClient.put(ApiEndpoints.Tasks.start(taskId)) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
        }

    /**
     * Submits a task solution.
     *
     * @param solutionUrl optional URL of the solution.
     * @param attachments list of attachment metadata maps.
     */
    suspend fun submitTask(
        cookie: String,
        taskId: String,
        solutionUrl: String? = null,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Boolean =
        safeApiAction(logger, "submit task taskId=$taskId") {
            httpClient.put(ApiEndpoints.Tasks.submit(taskId)) {
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

    /** @param lateDays number of late days to request. */
    suspend fun prolongLateDays(
        cookie: String,
        taskId: String,
        lateDays: Int,
    ): Boolean =
        safeApiAction(logger, "prolong late days for taskId=$taskId") {
            httpClient.put(ApiEndpoints.Tasks.lateDaysProlong(taskId)) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(mapOf("lateDays" to lateDays))
            }
        }

    /** Cancels late days for a task. */
    suspend fun cancelLateDays(
        cookie: String,
        taskId: String,
    ): Boolean =
        safeApiAction(logger, "cancel late days for taskId=$taskId") {
            httpClient.put(ApiEndpoints.Tasks.lateDaysCancel(taskId)) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
            }
        }

    /** @return the created comment's ID, or null on failure. */
    suspend fun createComment(
        cookie: String,
        taskId: String,
        content: String,
        attachments: List<MaterialAttachment> = emptyList(),
    ): String? =
        runCatchingCancellable {
            val response = httpClient.post(ApiEndpoints.Tasks.COMMENTS) {
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
            } else {
                logger.warn { "create comment for taskId=$taskId returned ${response.status}" }
                null
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to create comment for taskId=$taskId" }
            null
        }
}
