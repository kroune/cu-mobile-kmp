package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StartTaskResponse
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.data.network.TaskApiService
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.util.AppDispatchers

/**
 * Implementation of [TaskRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [TaskApiService].
 */
internal class TaskRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    taskApi: Lazy<TaskApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    TaskRepository {
    private val taskApi by taskApi

    override suspend fun fetchTasks(states: List<String>): List<StudentTask>? =
        withCookie { taskApi.fetchTasks(it, states) }

    override suspend fun fetchTaskDetails(taskId: String): TaskDetails? =
        withCookie { taskApi.fetchTaskDetails(it, taskId) }

    override suspend fun fetchTaskEvents(taskId: String): List<TaskEvent>? =
        withCookie { taskApi.fetchTaskEvents(it, taskId) }

    override suspend fun fetchTaskComments(taskId: String): List<TaskComment>? =
        withCookie { taskApi.fetchTaskComments(it, taskId) }

    override suspend fun startTask(taskId: String): StartTaskResponse? =
        withCookie { taskApi.startTask(it, taskId) }

    override suspend fun submitTask(
        taskId: String,
        solutionUrl: String?,
        attachments: List<MaterialAttachment>,
    ): Boolean =
        withCookieOrFalse { taskApi.submitTask(it, taskId, solutionUrl, attachments) }

    override suspend fun prolongLateDays(
        taskId: String,
        lateDays: Int,
    ): Boolean =
        withCookieOrFalse { taskApi.prolongLateDays(it, taskId, lateDays) }

    override suspend fun cancelLateDays(taskId: String): Boolean =
        withCookieOrFalse { taskApi.cancelLateDays(it, taskId) }

    override suspend fun createComment(
        taskId: String,
        content: String,
        attachments: List<MaterialAttachment>,
    ): String? =
        withCookie { taskApi.createComment(it, taskId, content, attachments) }

    override suspend fun editComment(
        commentId: String,
        content: String,
        attachments: List<MaterialAttachment>,
    ): Boolean =
        withCookieOrFalse { taskApi.editComment(it, commentId, content, attachments) }

    override suspend fun deleteComment(commentId: String): Boolean =
        withCookieOrFalse { taskApi.deleteComment(it, commentId) }
}
