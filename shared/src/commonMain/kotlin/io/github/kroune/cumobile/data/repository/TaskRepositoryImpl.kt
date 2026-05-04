package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toApi
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.TaskApiService
import io.github.kroune.cumobile.domain.model.MaterialAttachmentDomain
import io.github.kroune.cumobile.domain.model.StartTaskResponseDomain
import io.github.kroune.cumobile.domain.model.TaskCommentDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskEventDomain
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke

/**
 * Implementation of [TaskRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [TaskApiService].
 */
internal class TaskRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val taskApi: Lazy<TaskApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    TaskRepository {
    override suspend fun fetchTasks(states: List<String>): List<TaskDomain>? =
        withCookie { taskApi().fetchTasks(it, states)?.map { task -> task.toDomain() } }

    override suspend fun fetchTaskDetails(taskId: String): TaskDetailsDomain? =
        withCookie { taskApi().fetchTaskDetails(it, taskId) }?.toDomain()

    override suspend fun fetchTaskEvents(taskId: String): List<TaskEventDomain>? =
        withCookie { taskApi().fetchTaskEvents(it, taskId) }?.map { it.toDomain() }

    override suspend fun fetchTaskComments(taskId: String): List<TaskCommentDomain>? =
        withCookie { taskApi().fetchTaskComments(it, taskId) }?.map { it.toDomain() }

    override suspend fun startTask(taskId: String): StartTaskResponseDomain? =
        withCookie { taskApi().startTask(it, taskId) }?.toDomain()

    override suspend fun submitTask(
        taskId: String,
        solutionUrl: String?,
        attachments: List<MaterialAttachmentDomain>,
    ): Boolean =
        withCookieOrFalse { taskApi().submitTask(it, taskId, solutionUrl, attachments.map { a -> a.toApi() }) }

    override suspend fun prolongLateDays(
        taskId: String,
        lateDays: Int,
    ): Boolean =
        withCookieOrFalse { taskApi().prolongLateDays(it, taskId, lateDays) }

    override suspend fun cancelLateDays(taskId: String): Boolean =
        withCookieOrFalse { taskApi().cancelLateDays(it, taskId) }

    override suspend fun createComment(
        taskId: String,
        content: String,
        attachments: List<MaterialAttachmentDomain>,
    ): String? =
        withCookie { taskApi().createComment(it, taskId, content, attachments.map { a -> a.toApi() }) }

    override suspend fun editComment(
        commentId: String,
        content: String,
        attachments: List<MaterialAttachmentDomain>,
    ): Boolean =
        withCookieOrFalse { taskApi().editComment(it, commentId, content, attachments.map { a -> a.toApi() }) }

    override suspend fun deleteComment(commentId: String): Boolean =
        withCookieOrFalse { taskApi().deleteComment(it, commentId) }
}
