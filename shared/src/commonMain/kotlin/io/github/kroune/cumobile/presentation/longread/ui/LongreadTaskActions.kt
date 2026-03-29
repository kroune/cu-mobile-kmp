package io.github.kroune.cumobile.presentation.longread.ui

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Handles task mutation operations (start, submit, comment, late days)
 * for the longread component.
 */
internal class LongreadTaskActions(
    private val state: MutableValue<LongreadComponent.State>,
    private val effects: Channel<LongreadComponent.Effect>,
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope,
    private val loadTaskEventsAndComments: (String) -> Unit,
) {
    fun startTask() {
        val taskId = state.value.activeTaskId ?: return
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val result = taskRepository.startTask(taskId)
            state.value = state.value.copy(isSubmitting = false)
            if (result != null) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to start task $taskId" }
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось начать задание"),
                )
            }
        }
    }

    fun submitSolution() {
        val taskId = state.value.activeTaskId ?: return
        val url = state.value.solutionUrl.takeIf { it.isNotBlank() }
        val attachments = state.value.pendingSolutionAttachments
            .mapNotNull { it.uploadedAttachment }
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.submitTask(taskId, url, attachments)
            state.value = state.value.copy(isSubmitting = false)
            if (success) {
                state.value = state.value.copy(
                    solutionUrl = "",
                    pendingSolutionAttachments = persistentListOf(),
                )
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to submit solution for task $taskId" }
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отправить решение"),
                )
            }
        }
    }

    fun createComment() {
        val taskId = state.value.activeTaskId ?: return
        val text = state.value.commentText.trim()
        if (text.isEmpty()) return
        val attachments = state.value.pendingCommentAttachments
            .mapNotNull { it.uploadedAttachment }
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val commentId = taskRepository.createComment(taskId, text, attachments)
            if (commentId != null) {
                state.value = state.value.copy(
                    isSubmitting = false,
                    commentText = "",
                    pendingCommentAttachments = persistentListOf(),
                )
                val comments = taskRepository.fetchTaskComments(taskId)
                state.value = state.value.copy(
                    taskComments = comments.orEmpty().toPersistentList(),
                )
            } else {
                logger.warn { "Failed to create comment for task $taskId" }
                state.value = state.value.copy(isSubmitting = false)
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отправить комментарий"),
                )
            }
        }
    }

    fun saveEditComment() {
        val taskId = state.value.activeTaskId ?: return
        val commentId = state.value.editingCommentId ?: return
        val text = state.value.editCommentText.trim()
        if (text.isEmpty()) return
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.editComment(commentId, text)
            if (success) {
                state.value = state.value.copy(
                    isSubmitting = false,
                    editingCommentId = null,
                    editCommentText = "",
                )
                val comments = taskRepository.fetchTaskComments(taskId)
                state.value = state.value.copy(
                    taskComments = comments.orEmpty().toPersistentList(),
                )
            } else {
                logger.warn { "Failed to edit comment $commentId" }
                state.value = state.value.copy(isSubmitting = false)
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось изменить комментарий"),
                )
            }
        }
    }

    fun deleteComment(commentId: String) {
        val taskId = state.value.activeTaskId ?: return
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.deleteComment(commentId)
            if (success) {
                state.value = state.value.copy(isSubmitting = false)
                val comments = taskRepository.fetchTaskComments(taskId)
                state.value = state.value.copy(
                    taskComments = comments.orEmpty().toPersistentList(),
                )
            } else {
                logger.warn { "Failed to delete comment $commentId" }
                state.value = state.value.copy(isSubmitting = false)
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось удалить комментарий"),
                )
            }
        }
    }

    fun prolongLateDays(days: Int) {
        val taskId = state.value.activeTaskId ?: return
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.prolongLateDays(taskId, days)
            state.value = state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to prolong late days for task $taskId" }
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось продлить дедлайн"),
                )
            }
        }
    }

    fun cancelLateDays() {
        val taskId = state.value.activeTaskId ?: return
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.cancelLateDays(taskId)
            state.value = state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to cancel late days for task $taskId" }
                effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отменить продление"),
                )
            }
        }
    }

    private suspend fun refreshTaskDetails(taskId: String) {
        val details = taskRepository.fetchTaskDetails(taskId)
        if (details != null) {
            val updated = state.value.taskDetails.toMutableMap()
            updated[taskId] = details
            state.value = state.value.copy(taskDetails = updated.toPersistentMap())
        }
        loadTaskEventsAndComments(taskId)
    }
}
