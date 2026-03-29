package io.github.kroune.cumobile.presentation.longread.component.coding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.longread.ui.coding.CodingMaterialCardContent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [CodingMaterialComponent].
 *
 * Owns all task-specific state and operations for a single coding exercise.
 * Lifecycle is managed by the parent's ChildItems framework.
 */
class DefaultCodingMaterialComponent(
    componentContext: ComponentContext,
    override val material: LongreadMaterial,
    private val taskId: String,
    private val taskRepository: TaskRepository,
    contentRepository: ContentRepository,
    private val onShowError: (String) -> Unit,
) : CodingMaterialComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(CodingMaterialComponent.State())
    override val state: Value<CodingMaterialComponent.State> = _state

    private val taskActions = CodingTaskActions(
        taskId = taskId,
        state = _state,
        taskRepository = taskRepository,
        scope = scope,
        onShowError = onShowError,
    )

    private val attachmentManager = CodingAttachmentManager(
        taskId = taskId,
        state = _state,
        contentRepository = contentRepository,
        scope = scope,
    )

    init {
        loadTaskDetails()
    }

    override fun onIntent(intent: CodingMaterialComponent.Intent) {
        when (intent) {
            CodingMaterialComponent.Intent.ToggleExpanded -> toggleExpanded()
            is CodingMaterialComponent.Intent.SelectTab ->
                _state.value = _state.value.copy(selectedTab = intent.tab)
            is CodingMaterialComponent.Intent.Task -> handleTaskIntent(intent)
            is CodingMaterialComponent.Intent.Comment -> handleCommentIntent(intent)
            is CodingMaterialComponent.Intent.Attachment -> handleAttachmentIntent(intent)
        }
    }

    private fun handleTaskIntent(intent: CodingMaterialComponent.Intent.Task) {
        when (intent) {
            CodingMaterialComponent.Intent.Task.StartTask -> taskActions.startTask()
            CodingMaterialComponent.Intent.Task.SubmitSolution -> taskActions.submitSolution()
            CodingMaterialComponent.Intent.Task.CancelLateDays -> taskActions.cancelLateDays()
            is CodingMaterialComponent.Intent.Task.UpdateSolutionUrl ->
                _state.value = _state.value.copy(solutionUrl = intent.url)
            is CodingMaterialComponent.Intent.Task.ProlongLateDays ->
                taskActions.prolongLateDays(intent.days)
        }
    }

    private fun handleCommentIntent(intent: CodingMaterialComponent.Intent.Comment) {
        when (intent) {
            CodingMaterialComponent.Intent.Comment.CreateComment ->
                taskActions.createComment()
            CodingMaterialComponent.Intent.Comment.SaveEditComment ->
                taskActions.saveEditComment()
            CodingMaterialComponent.Intent.Comment.CancelEditComment ->
                _state.value = _state.value.copy(
                    editingCommentId = null,
                    editCommentText = "",
                )
            is CodingMaterialComponent.Intent.Comment.UpdateCommentText ->
                _state.value = _state.value.copy(commentText = intent.text)
            is CodingMaterialComponent.Intent.Comment.StartEditComment ->
                _state.value = _state.value.copy(
                    editingCommentId = intent.commentId,
                    editCommentText = intent.currentText,
                )
            is CodingMaterialComponent.Intent.Comment.UpdateEditCommentText ->
                _state.value = _state.value.copy(editCommentText = intent.text)
            is CodingMaterialComponent.Intent.Comment.DeleteComment ->
                taskActions.deleteComment(intent.commentId)
        }
    }

    private fun handleAttachmentIntent(intent: CodingMaterialComponent.Intent.Attachment) {
        when (intent) {
            is CodingMaterialComponent.Intent.Attachment.PickSolutionAttachment ->
                attachmentManager.uploadAttachment(intent.file, isSolution = true)
            is CodingMaterialComponent.Intent.Attachment.RemoveSolutionAttachment ->
                attachmentManager.removeSolutionAttachment(intent.index)
            is CodingMaterialComponent.Intent.Attachment.PickCommentAttachment ->
                attachmentManager.uploadAttachment(intent.file, isSolution = false)
            is CodingMaterialComponent.Intent.Attachment.RemoveCommentAttachment ->
                attachmentManager.removeCommentAttachment(intent.index)
        }
    }

    private fun toggleExpanded() {
        val wasExpanded = _state.value.isExpanded
        _state.value = _state.value.copy(isExpanded = !wasExpanded)
        if (!wasExpanded) {
            loadEventsAndComments()
        }
    }

    private fun loadTaskDetails() {
        scope.launch {
            val details = taskRepository.fetchTaskDetails(taskId)
            if (details != null) {
                _state.value = _state.value.copy(taskDetails = details)
            } else {
                logger.warn { "Failed to load task details for taskId=$taskId" }
            }
        }
    }

    private fun loadEventsAndComments() {
        scope.launch {
            coroutineScope {
                val eventsDeferred = async { taskRepository.fetchTaskEvents(taskId) }
                val commentsDeferred = async { taskRepository.fetchTaskComments(taskId) }
                val events = eventsDeferred.await()
                val comments = commentsDeferred.await()
                if (events == null) {
                    logger.warn { "Failed to load task events for taskId=$taskId" }
                }
                if (comments == null) {
                    logger.warn { "Failed to load task comments for taskId=$taskId" }
                }
                _state.value = _state.value.copy(
                    taskEvents = events.orEmpty().toPersistentList(),
                    taskComments = comments.orEmpty().toPersistentList(),
                )
            }
        }
    }

    @Composable
    override fun Render() {
        val componentState by state.subscribeAsState()
        CodingMaterialCardContent(
            material = material,
            state = componentState,
            onIntent = ::onIntent,
        )
    }
}
