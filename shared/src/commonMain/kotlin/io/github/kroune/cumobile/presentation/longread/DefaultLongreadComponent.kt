package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.longread.LongreadComponent.Intent
import io.github.kroune.cumobile.presentation.longread.ui.LongreadTaskActions
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [LongreadComponent].
 *
 * Loads materials for a longread and manages task actions
 * (start, submit, comments, late days) for coding materials.
 */
class DefaultLongreadComponent(
    componentContext: ComponentContext,
    params: LongreadParams,
    deps: LongreadDependencies,
    private val onBack: () -> Unit,
    private val onDownloadReady: suspend (url: String, filename: String) -> Boolean,
    private val onNavigateToFiles: () -> Unit,
) : LongreadComponent,
    ComponentContext by componentContext {
    private val contentRepository = deps.contentRepository
    private val taskRepository = deps.taskRepository
    private val renameRepository = deps.renameRepository
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(
        LongreadComponent.State(
            longreadId = params.longreadId,
            courseId = params.courseId,
            themeId = params.themeId,
        ),
    )
    override val state: Value<LongreadComponent.State> = _state

    private val _effects = Channel<LongreadComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<LongreadComponent.Effect> = _effects.receiveAsFlow()

    private val taskActions = LongreadTaskActions(
        state = _state,
        effects = _effects,
        taskRepository = taskRepository,
        scope = scope,
        loadTaskEventsAndComments = ::loadTaskEventsAndComments,
    )

    private val attachmentManager = LongreadAttachmentManager(
        state = _state,
        contentRepository = contentRepository,
        scope = scope,
    )

    private val searchHandler = LongreadSearchHandler(state = _state)

    override fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.Navigation -> handleNavigationIntent(intent)
            is Intent.Task -> handleTaskIntent(intent)
            is Intent.Comment -> handleCommentIntent(intent)
            is Intent.Attachment -> handleAttachmentIntent(intent)
            is Intent.Search -> handleSearchIntent(intent)
        }
    }

    private fun handleNavigationIntent(intent: Intent.Navigation) {
        when (intent) {
            Intent.Navigation.Back -> onBack()
            Intent.Navigation.Refresh -> loadMaterials()
            Intent.Navigation.NavigateToFiles -> onNavigateToFiles()
            is Intent.Navigation.SelectTask -> selectTask(intent.taskId)
            is Intent.Navigation.SelectTaskTab ->
                _state.value = _state.value.copy(selectedTaskTab = intent.tab)

            is Intent.Navigation.DownloadFile ->
                downloadFile(intent.material)
        }
    }

    private fun handleTaskIntent(intent: Intent.Task) {
        when (intent) {
            Intent.Task.StartTask -> taskActions.startTask()
            Intent.Task.SubmitSolution -> taskActions.submitSolution()
            Intent.Task.CancelLateDays -> taskActions.cancelLateDays()
            is Intent.Task.UpdateSolutionUrl ->
                _state.value = _state.value.copy(solutionUrl = intent.url)

            is Intent.Task.ProlongLateDays ->
                taskActions.prolongLateDays(intent.days)
        }
    }

    private fun handleCommentIntent(intent: Intent.Comment) {
        when (intent) {
            Intent.Comment.CreateComment -> taskActions.createComment()
            Intent.Comment.SaveEditComment -> taskActions.saveEditComment()
            Intent.Comment.CancelEditComment ->
                _state.value = _state.value.copy(
                    editingCommentId = null,
                    editCommentText = "",
                )

            is Intent.Comment.UpdateCommentText ->
                _state.value = _state.value.copy(commentText = intent.text)

            is Intent.Comment.StartEditComment ->
                _state.value = _state.value.copy(
                    editingCommentId = intent.commentId,
                    editCommentText = intent.currentText,
                )

            is Intent.Comment.UpdateEditCommentText ->
                _state.value = _state.value.copy(editCommentText = intent.text)

            is Intent.Comment.DeleteComment ->
                taskActions.deleteComment(intent.commentId)
        }
    }

    private fun handleAttachmentIntent(intent: Intent.Attachment) {
        when (intent) {
            is Intent.Attachment.PickSolutionAttachment ->
                attachmentManager.uploadAttachment(intent.file, isSolution = true)

            is Intent.Attachment.RemoveSolutionAttachment ->
                attachmentManager.removeSolutionAttachment(intent.index)

            is Intent.Attachment.PickCommentAttachment ->
                attachmentManager.uploadAttachment(intent.file, isSolution = false)

            is Intent.Attachment.RemoveCommentAttachment ->
                attachmentManager.removeCommentAttachment(intent.index)
        }
    }

    private fun handleSearchIntent(intent: Intent.Search) {
        when (intent) {
            Intent.Search.ToggleSearch -> searchHandler.toggleSearch()
            Intent.Search.NextMatch ->
                searchHandler.navigateMatch(forward = true)

            Intent.Search.PreviousMatch ->
                searchHandler.navigateMatch(forward = false)

            is Intent.Search.UpdateSearchQuery ->
                searchHandler.updateSearchQuery(intent.query)
        }
    }

    init {
        loadMaterials()
    }

    private fun loadMaterials() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val materials = contentRepository.fetchLongreadMaterials(
                _state.value.longreadId,
            )
            if (materials != null) {
                _state.value = _state.value.copy(
                    materials = materials.toPersistentList(),
                    isLoading = false,
                )
                loadTaskDetailsForCodingMaterials(materials)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить материалы",
                )
            }
        }
    }

    private suspend fun loadTaskDetailsForCodingMaterials(materials: List<LongreadMaterial>) {
        val codingTaskIds = materials
            .filter { it.isCoding && it.taskId != null }
            .mapNotNull { it.taskId }
        coroutineScope {
            val deferredDetails = codingTaskIds.map { taskId ->
                async { taskId to taskRepository.fetchTaskDetails(taskId) }
            }
            val detailsMap = _state.value.taskDetails.toMutableMap()
            for (deferred in deferredDetails) {
                val (taskId, details) = deferred.await()
                if (details != null) {
                    detailsMap[taskId] = details
                    _state.value = _state.value.copy(taskDetails = detailsMap.toPersistentMap())
                } else {
                    logger.warn { "Failed to load task details for taskId=$taskId" }
                }
            }
        }
    }

    private fun selectTask(taskId: String) {
        val isToggleOff = _state.value.activeTaskId == taskId
        _state.value = _state.value.copy(
            activeTaskId = if (isToggleOff) null else taskId,
            selectedTaskTab = "solution",
            solutionUrl = "",
            commentText = "",
            taskEvents = persistentListOf(),
            taskComments = persistentListOf(),
            pendingSolutionAttachments = persistentListOf(),
            pendingCommentAttachments = persistentListOf(),
        )
        if (!isToggleOff) {
            loadTaskEventsAndComments(taskId)
        }
    }

    private fun loadTaskEventsAndComments(taskId: String) {
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

    private fun downloadFile(material: LongreadMaterial) {
        val filename = material.filename ?: return
        val version = material.version ?: "1"
        scope.launch {
            _effects.trySend(
                LongreadComponent.Effect.ShowSuccess("Скачивание..."),
            )
            val url = contentRepository.getDownloadLink(filename, version)
            if (url != null) {
                val localFilename = buildLocalFilename(material)
                val saved = onDownloadReady(url, localFilename)
                if (saved) {
                    _effects.trySend(
                        LongreadComponent.Effect.ShowSuccess(
                            "Файл сохранён в «Файлы»",
                        ),
                    )
                } else {
                    _effects.trySend(
                        LongreadComponent.Effect.ShowError(
                            "Не удалось сохранить файл",
                        ),
                    )
                }
            } else {
                logger.warn { "Failed to get download link for $filename" }
                _effects.trySend(
                    LongreadComponent.Effect.ShowError(
                        "Не удалось получить ссылку для скачивания",
                    ),
                )
            }
        }
    }

    /**
     * Builds a local filename for a material, applying rename templates if available.
     *
     * Fallback format: `{name}_{version}.{ext}`
     */
    private suspend fun buildLocalFilename(material: LongreadMaterial): String {
        val filename = material.filename ?: "file"
        val version = material.version ?: "1"
        val dotIndex = filename.lastIndexOf('.')
        val extension = if (dotIndex > 0) filename.substring(dotIndex + 1) else ""

        val activityName = material.estimation?.activityName
        if (activityName != null) {
            val rule = renameRepository.getMatchingRule(
                courseId = _state.value.courseId,
                activityName = activityName,
                extension = extension,
            )
            if (rule != null) {
                return rule.apply(
                    courseName = _state.value.title,
                    activityName = activityName,
                    version = version,
                )
            }
        }

        val baseName = if (dotIndex > 0) filename.substring(0, dotIndex) else filename
        val safeName = baseName.replace(UNSAFE_CHARS_REGEX, "_")
        val extPart = if (extension.isNotEmpty()) ".$extension" else ""
        return "${safeName}_$version$extPart"
    }

    companion object {
        private val UNSAFE_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")
    }
}
