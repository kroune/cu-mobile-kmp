package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent

/** Repository for task-related operations. */
interface TaskRepository {
    /**
     * Fetches tasks filtered by the given state values.
     *
     * Known states: "inProgress", "review", "backlog", "failed", "evaluated".
     */
    suspend fun fetchTasks(states: List<String>): List<StudentTask>?

    /** Fetches details for a specific task. */
    suspend fun fetchTaskDetails(taskId: Int): TaskDetails?

    /** Fetches the event history for a task. */
    suspend fun fetchTaskEvents(taskId: Int): List<TaskEvent>?

    /** Fetches comments on a task. */
    suspend fun fetchTaskComments(taskId: Int): List<TaskComment>?

    /** Starts a task (moves it from backlog to inProgress). */
    suspend fun startTask(taskId: Int): Boolean

    /**
     * Submits a task solution.
     *
     * @param solutionUrl optional URL of the solution (e.g. GitHub link).
     * @param attachments optional list of file attachments.
     */
    suspend fun submitTask(
        taskId: Int,
        solutionUrl: String? = null,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Boolean

    /** Requests late-day extension for a task. */
    suspend fun prolongLateDays(
        taskId: Int,
        lateDays: Int,
    ): Boolean

    /** Cancels an active late-day extension. */
    suspend fun cancelLateDays(taskId: Int): Boolean

    /**
     * Creates a comment on a task.
     *
     * @return the created comment's ID, or null on failure.
     */
    suspend fun createComment(
        taskId: Int,
        content: String,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Int?
}
