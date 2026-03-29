package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.StartTaskResponse
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
    suspend fun fetchTaskDetails(taskId: String): TaskDetails?

    /** Fetches the event history for a task. */
    suspend fun fetchTaskEvents(taskId: String): List<TaskEvent>?

    /** Fetches comments on a task. */
    suspend fun fetchTaskComments(taskId: String): List<TaskComment>?

    /** Starts a task (moves it from backlog to inProgress). */
    suspend fun startTask(taskId: String): StartTaskResponse?

    /**
     * Submits a task solution.
     *
     * @param solutionUrl optional URL of the solution (e.g. GitHub link).
     * @param attachments optional list of file attachments.
     */
    suspend fun submitTask(
        taskId: String,
        solutionUrl: String? = null,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Boolean

    /** Requests late-day extension for a task. */
    suspend fun prolongLateDays(
        taskId: String,
        lateDays: Int,
    ): Boolean

    /** Cancels an active late-day extension. */
    suspend fun cancelLateDays(taskId: String): Boolean

    /**
     * Creates a comment on a task.
     *
     * @return the created comment's ID, or null on failure.
     */
    suspend fun createComment(
        taskId: String,
        content: String,
        attachments: List<MaterialAttachment> = emptyList(),
    ): String?

    /** Updates an existing comment's text and attachments. */
    suspend fun editComment(
        commentId: String,
        content: String,
        attachments: List<MaterialAttachment> = emptyList(),
    ): Boolean

    /** Deletes a comment by its ID. */
    suspend fun deleteComment(commentId: String): Boolean
}
