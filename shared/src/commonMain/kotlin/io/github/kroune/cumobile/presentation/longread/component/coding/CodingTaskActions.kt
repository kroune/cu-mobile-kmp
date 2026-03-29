package io.github.kroune.cumobile.presentation.longread.component.coding

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Handles task mutation operations for a single [CodingMaterialComponent].
 *
 * Adapted from the former LongreadTaskActions but scoped to one task.
 */
internal class CodingTaskActions(
    private val taskId: String,
    private val state: MutableValue<CodingMaterialComponent.State>,
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope,
    private val onShowError: (String) -> Unit,
) {
    fun startTask() {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val result = taskRepository.startTask(taskId)
            state.value = state.value.copy(isSubmitting = false)
            if (result != null) {
                refreshTaskDetails()
            } else {
                logger.warn { "Failed to start task $taskId" }
                onShowError("Не удалось начать задание")
            }
        }
    }

    fun submitSolution() {
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
                refreshTaskDetails()
            } else {
                logger.warn { "Failed to submit solution for task $taskId" }
                onShowError("Не удалось отправить решение")
            }
        }
    }

    fun createComment() {
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
                refreshComments()
            } else {
                logger.warn { "Failed to create comment for task $taskId" }
                state.value = state.value.copy(isSubmitting = false)
                onShowError("Не удалось отправить комментарий")
            }
        }
    }

    fun saveEditComment() {
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
                refreshComments()
            } else {
                logger.warn { "Failed to edit comment $commentId" }
                state.value = state.value.copy(isSubmitting = false)
                onShowError("Не удалось изменить комментарий")
            }
        }
    }

    fun deleteComment(commentId: String) {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.deleteComment(commentId)
            if (success) {
                state.value = state.value.copy(isSubmitting = false)
                refreshComments()
            } else {
                logger.warn { "Failed to delete comment $commentId" }
                state.value = state.value.copy(isSubmitting = false)
                onShowError("Не удалось удалить комментарий")
            }
        }
    }

    fun prolongLateDays(days: Int) {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.prolongLateDays(taskId, days)
            state.value = state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails()
            } else {
                logger.warn { "Failed to prolong late days for task $taskId" }
                onShowError("Не удалось продлить дедлайн")
            }
        }
    }

    fun cancelLateDays() {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val success = taskRepository.cancelLateDays(taskId)
            state.value = state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails()
            } else {
                logger.warn { "Failed to cancel late days for task $taskId" }
                onShowError("Не удалось отменить продление")
            }
        }
    }

    private suspend fun refreshTaskDetails() {
        val details = taskRepository.fetchTaskDetails(taskId)
        if (details != null) {
            state.value = state.value.copy(taskDetails = details)
        }
        refreshEventsAndComments()
    }

    private suspend fun refreshEventsAndComments() {
        val events = taskRepository.fetchTaskEvents(taskId)
        val comments = taskRepository.fetchTaskComments(taskId)
        if (events == null) {
            logger.warn { "Failed to load task events for taskId=$taskId" }
        }
        if (comments == null) {
            logger.warn { "Failed to load task comments for taskId=$taskId" }
        }
        state.value = state.value.copy(
            taskEvents = events.orEmpty().toPersistentList(),
            taskComments = comments.orEmpty().toPersistentList(),
        )
    }

    private suspend fun refreshComments() {
        val comments = taskRepository.fetchTaskComments(taskId)
        state.value = state.value.copy(
            taskComments = comments.orEmpty().toPersistentList(),
        )
    }
}
