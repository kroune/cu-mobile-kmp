package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.TaskRepository

/**
 * Implementation of [TaskRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class TaskRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    TaskRepository {
    override suspend fun fetchTasks(states: List<String>): List<StudentTask>? =
        withCookie {
            apiService.fetchTasks(it, states)
        }

    override suspend fun fetchTaskDetails(taskId: Int): TaskDetails? =
        withCookie {
            apiService.fetchTaskDetails(it, taskId)
        }

    override suspend fun fetchTaskEvents(taskId: Int): List<TaskEvent>? =
        withCookie {
            apiService.fetchTaskEvents(it, taskId)
        }

    override suspend fun fetchTaskComments(taskId: Int): List<TaskComment>? =
        withCookie {
            apiService.fetchTaskComments(it, taskId)
        }

    override suspend fun startTask(taskId: Int): Boolean =
        withCookieOrFalse {
            apiService.startTask(it, taskId)
        }

    override suspend fun submitTask(
        taskId: Int,
        solutionUrl: String?,
        attachments: List<MaterialAttachment>,
    ): Boolean =
        withCookieOrFalse {
            apiService.submitTask(it, taskId, solutionUrl, attachments)
        }

    override suspend fun prolongLateDays(
        taskId: Int,
        lateDays: Int,
    ): Boolean =
        withCookieOrFalse {
            apiService.prolongLateDays(it, taskId, lateDays)
        }

    override suspend fun cancelLateDays(taskId: Int): Boolean =
        withCookieOrFalse {
            apiService.cancelLateDays(it, taskId)
        }

    override suspend fun createComment(
        taskId: Int,
        content: String,
        attachments: List<MaterialAttachment>,
    ): Int? =
        withCookie {
            apiService.createComment(it, taskId, content, attachments)
        }
}
