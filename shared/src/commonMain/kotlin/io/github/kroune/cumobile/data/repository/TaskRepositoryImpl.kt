package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [TaskRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class TaskRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : TaskRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchTasks(states: List<String>): List<StudentTask>? {
        val c = cookie() ?: return null
        return apiService.fetchTasks(c, states)
    }

    override suspend fun fetchTaskDetails(taskId: Int): TaskDetails? {
        val c = cookie() ?: return null
        return apiService.fetchTaskDetails(c, taskId)
    }

    override suspend fun fetchTaskEvents(taskId: Int): List<TaskEvent>? {
        val c = cookie() ?: return null
        return apiService.fetchTaskEvents(c, taskId)
    }

    override suspend fun fetchTaskComments(taskId: Int): List<TaskComment>? {
        val c = cookie() ?: return null
        return apiService.fetchTaskComments(c, taskId)
    }

    override suspend fun startTask(taskId: Int): Boolean {
        val c = cookie() ?: return false
        return apiService.startTask(c, taskId)
    }

    override suspend fun submitTask(
        taskId: Int,
        solutionUrl: String?,
        attachments: List<MaterialAttachment>,
    ): Boolean {
        val c = cookie() ?: return false
        return apiService.submitTask(c, taskId, solutionUrl, attachments)
    }

    override suspend fun prolongLateDays(
        taskId: Int,
        lateDays: Int,
    ): Boolean {
        val c = cookie() ?: return false
        return apiService.prolongLateDays(c, taskId, lateDays)
    }

    override suspend fun cancelLateDays(taskId: Int): Boolean {
        val c = cookie() ?: return false
        return apiService.cancelLateDays(c, taskId)
    }

    override suspend fun createComment(
        taskId: Int,
        content: String,
        attachments: List<MaterialAttachment>,
    ): Int? {
        val c = cookie() ?: return null
        return apiService.createComment(c, taskId, content, attachments)
    }
}
