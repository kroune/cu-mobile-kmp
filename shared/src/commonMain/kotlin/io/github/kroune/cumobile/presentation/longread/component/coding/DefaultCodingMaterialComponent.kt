package io.github.kroune.cumobile.presentation.longread.component.coding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.longread.ui.coding.CodingMaterialCardContent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentList
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
    private val initiallyExpanded: Boolean = false,
    private val taskRepository: TaskRepository,
    private val contentRepository: ContentRepository,
    private val onShowError: (String) -> Unit,
    private val onSaveFile: suspend (url: String, filename: String) -> Boolean = { _, _ -> false },
) : CodingMaterialComponent,
    ComponentContext by componentContext {
    private val scope = componentScope()

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
        if (initiallyExpanded) {
            _state.value = _state.value.copy(isExpanded = true)
            loadEventsAndComments()
        }
    }

    override fun onIntent(intent: CodingMaterialComponent.Intent) {
        when (intent) {
            CodingMaterialComponent.Intent.ToggleExpanded -> toggleExpanded()
            CodingMaterialComponent.Intent.RetryLoadDetails -> loadTaskDetails()
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
            is CodingMaterialComponent.Intent.Attachment.DownloadCommentAttachment ->
                downloadCommentAttachment(intent.attachment)
        }
    }

    private fun downloadCommentAttachment(attachment: MaterialAttachment) {
        scope.launch {
            _state.value = _state.value.copy(downloadingAttachment = attachment.filename)
            val url = contentRepository.getDownloadLink(attachment.filename, attachment.version)
            if (url != null) {
                val saved = onSaveFile(url, attachment.name)
                if (!saved) {
                    onShowError("Не удалось сохранить файл")
                }
            } else {
                logger.warn { "Failed to get download link for attachment: ${attachment.filename}" }
                onShowError("Не удалось получить ссылку для скачивания")
            }
            _state.value = _state.value.copy(downloadingAttachment = null)
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
            _state.value = _state.value.copy(taskDetails = ContentState.Loading)
            val details = taskRepository.fetchTaskDetails(taskId)
            _state.value = if (details != null) {
                _state.value.copy(taskDetails = ContentState.Success(details))
            } else {
                logger.warn { "Failed to load task details for taskId=$taskId" }
                _state.value.copy(
                    taskDetails = ContentState.Error("Не удалось загрузить задание"),
                )
            }
        }
    }

    private fun loadEventsAndComments() {
        scope.launch {
            _state.value = _state.value.copy(
                taskEvents = ContentState.Loading,
                taskComments = ContentState.Loading,
            )
            coroutineScope {
                val eventsDeferred = async { taskRepository.fetchTaskEvents(taskId) }
                val commentsDeferred = async { taskRepository.fetchTaskComments(taskId) }
                val events = eventsDeferred.await()
                val comments = commentsDeferred.await()
                val eventsState = if (events != null) {
                    ContentState.Success(events.toPersistentList())
                } else {
                    logger.warn { "Failed to load task events for taskId=$taskId" }
                    ContentState.Error("Не удалось загрузить историю")
                }
                val commentsState = if (comments != null) {
                    ContentState.Success(comments.toPersistentList())
                } else {
                    logger.warn { "Failed to load task comments for taskId=$taskId" }
                    ContentState.Error("Не удалось загрузить комментарии")
                }
                _state.value = _state.value.copy(
                    taskEvents = eventsState,
                    taskComments = commentsState,
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
