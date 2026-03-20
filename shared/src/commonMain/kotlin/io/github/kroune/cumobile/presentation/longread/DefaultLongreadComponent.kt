package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.model.UploadStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
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
@Suppress("TooManyFunctions")
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

    override fun onIntent(intent: LongreadComponent.Intent) {
        when (intent) {
            LongreadComponent.Intent.Back -> onBack()
            LongreadComponent.Intent.Refresh -> loadMaterials()
            is LongreadComponent.Intent.SelectTask -> selectTask(intent.taskId)
            is LongreadComponent.Intent.SelectTaskTab ->
                _state.value = _state.value.copy(selectedTaskTab = intent.tab)
            is LongreadComponent.Intent.UpdateSolutionUrl ->
                _state.value = _state.value.copy(solutionUrl = intent.url)
            is LongreadComponent.Intent.UpdateCommentText ->
                _state.value = _state.value.copy(commentText = intent.text)
            is LongreadComponent.Intent.DownloadFile ->
                downloadFile(intent.material)
            LongreadComponent.Intent.StartTask,
            LongreadComponent.Intent.SubmitSolution,
            LongreadComponent.Intent.CreateComment,
            is LongreadComponent.Intent.ProlongLateDays,
            LongreadComponent.Intent.CancelLateDays,
            -> handleTaskActionIntent(intent)
            is LongreadComponent.Intent.PickSolutionAttachment,
            is LongreadComponent.Intent.RemoveSolutionAttachment,
            is LongreadComponent.Intent.PickCommentAttachment,
            is LongreadComponent.Intent.RemoveCommentAttachment,
            -> handleAttachmentIntent(intent)
            is LongreadComponent.Intent.ToggleSearch,
            is LongreadComponent.Intent.UpdateSearchQuery,
            is LongreadComponent.Intent.NextMatch,
            is LongreadComponent.Intent.PreviousMatch,
            -> handleSearchIntent(intent)
            LongreadComponent.Intent.NavigateToFiles -> onNavigateToFiles()
        }
    }

    private fun handleTaskActionIntent(intent: LongreadComponent.Intent) {
        when (intent) {
            LongreadComponent.Intent.StartTask -> startTask()
            LongreadComponent.Intent.SubmitSolution -> submitSolution()
            LongreadComponent.Intent.CreateComment -> createComment()
            is LongreadComponent.Intent.ProlongLateDays -> prolongLateDays(intent.days)
            LongreadComponent.Intent.CancelLateDays -> cancelLateDays()
            else -> Unit
        }
    }

    private fun handleAttachmentIntent(intent: LongreadComponent.Intent) {
        when (intent) {
            is LongreadComponent.Intent.PickSolutionAttachment ->
                uploadAttachment(intent.file, isSolution = true)
            is LongreadComponent.Intent.RemoveSolutionAttachment ->
                removeSolutionAttachment(intent.index)
            is LongreadComponent.Intent.PickCommentAttachment ->
                uploadAttachment(intent.file, isSolution = false)
            is LongreadComponent.Intent.RemoveCommentAttachment ->
                removeCommentAttachment(intent.index)
            else -> Unit
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
                    materials = materials,
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
        val detailsMap = _state.value.taskDetails.toMutableMap()
        for (taskId in codingTaskIds) {
            val details = taskRepository.fetchTaskDetails(taskId)
            if (details != null) {
                detailsMap[taskId] = details
            }
        }
        _state.value = _state.value.copy(taskDetails = detailsMap)
    }

    private fun selectTask(taskId: Int) {
        val isToggleOff = _state.value.activeTaskId == taskId
        _state.value = _state.value.copy(
            activeTaskId = if (isToggleOff) null else taskId,
            selectedTaskTab = "solution",
            solutionUrl = "",
            commentText = "",
            taskEvents = emptyList(),
            taskComments = emptyList(),
            pendingSolutionAttachments = emptyList(),
            pendingCommentAttachments = emptyList(),
        )
        if (!isToggleOff) {
            loadTaskEventsAndComments(taskId)
        }
    }

    private fun loadTaskEventsAndComments(taskId: Int) {
        scope.launch {
            val events = taskRepository.fetchTaskEvents(taskId)
            val comments = taskRepository.fetchTaskComments(taskId)
            _state.value = _state.value.copy(
                taskEvents = events.orEmpty(),
                taskComments = comments.orEmpty(),
            )
        }
    }

    private fun startTask() {
        val taskId = _state.value.activeTaskId ?: return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val success = taskRepository.startTask(taskId)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to start task $taskId" }
                _effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось начать задание"),
                )
            }
        }
    }

    private fun submitSolution() {
        val taskId = _state.value.activeTaskId ?: return
        val url = _state.value.solutionUrl.takeIf { it.isNotBlank() }
        val attachments = _state.value.pendingSolutionAttachments
            .mapNotNull { it.uploadedAttachment }
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val success = taskRepository.submitTask(taskId, url, attachments)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                _state.value = _state.value.copy(
                    solutionUrl = "",
                    pendingSolutionAttachments = emptyList(),
                )
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to submit solution for task $taskId" }
                _effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отправить решение"),
                )
            }
        }
    }

    private fun createComment() {
        val taskId = _state.value.activeTaskId ?: return
        val text = _state.value.commentText.trim()
        if (text.isEmpty()) return
        val attachments = _state.value.pendingCommentAttachments
            .mapNotNull { it.uploadedAttachment }
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val commentId = taskRepository.createComment(taskId, text, attachments)
            if (commentId != null) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    commentText = "",
                    pendingCommentAttachments = emptyList(),
                )
                val comments = taskRepository.fetchTaskComments(taskId)
                _state.value = _state.value.copy(
                    taskComments = comments.orEmpty(),
                )
            } else {
                logger.warn { "Failed to create comment for task $taskId" }
                _state.value = _state.value.copy(isSubmitting = false)
                _effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отправить комментарий"),
                )
            }
        }
    }

    private fun prolongLateDays(days: Int) {
        val taskId = _state.value.activeTaskId ?: return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val success = taskRepository.prolongLateDays(taskId, days)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to prolong late days for task $taskId" }
                _effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось продлить дедлайн"),
                )
            }
        }
    }

    private fun cancelLateDays() {
        val taskId = _state.value.activeTaskId ?: return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val success = taskRepository.cancelLateDays(taskId)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                refreshTaskDetails(taskId)
            } else {
                logger.warn { "Failed to cancel late days for task $taskId" }
                _effects.trySend(
                    LongreadComponent.Effect.ShowError("Не удалось отменить продление"),
                )
            }
        }
    }

    private fun uploadAttachment(
        file: PickedFile,
        isSolution: Boolean,
    ) {
        val taskId = _state.value.activeTaskId ?: return
        val pending = PendingAttachment(
            name = file.name,
            size = file.size,
            status = UploadStatus.Uploading,
        )
        if (isSolution) {
            _state.value = _state.value.copy(
                pendingSolutionAttachments =
                    _state.value.pendingSolutionAttachments + pending,
            )
        } else {
            _state.value = _state.value.copy(
                pendingCommentAttachments =
                    _state.value.pendingCommentAttachments + pending,
            )
        }
        val directory = if (isSolution) {
            "tasks/$taskId/solutions"
        } else {
            "tasks/$taskId/comments"
        }
        scope.launch {
            val attachment = contentRepository.uploadFile(
                directory = directory,
                filename = file.name,
                contentType = file.contentType,
                bytes = file.bytes,
            )
            if (isSolution) {
                updateSolutionAttachment(file.name, attachment)
            } else {
                updateCommentAttachment(file.name, attachment)
            }
        }
    }

    private fun updateSolutionAttachment(
        fileName: String,
        attachment: io.github.kroune.cumobile.data.model.MaterialAttachment?,
    ) {
        val list = _state.value.pendingSolutionAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatus.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatus.Uploaded else UploadStatus.Failed,
                uploadedAttachment = attachment,
            )
            _state.value = _state.value.copy(pendingSolutionAttachments = list)
        }
    }

    private fun updateCommentAttachment(
        fileName: String,
        attachment: io.github.kroune.cumobile.data.model.MaterialAttachment?,
    ) {
        val list = _state.value.pendingCommentAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatus.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatus.Uploaded else UploadStatus.Failed,
                uploadedAttachment = attachment,
            )
            _state.value = _state.value.copy(pendingCommentAttachments = list)
        }
    }

    private fun removeSolutionAttachment(index: Int) {
        val list = _state.value.pendingSolutionAttachments.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(pendingSolutionAttachments = list)
        }
    }

    private fun removeCommentAttachment(index: Int) {
        val list = _state.value.pendingCommentAttachments.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(pendingCommentAttachments = list)
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

    private fun handleSearchIntent(intent: LongreadComponent.Intent) {
        when (intent) {
            LongreadComponent.Intent.ToggleSearch -> toggleSearch()
            is LongreadComponent.Intent.UpdateSearchQuery ->
                updateSearchQuery(intent.query)
            LongreadComponent.Intent.NextMatch -> navigateMatch(forward = true)
            LongreadComponent.Intent.PreviousMatch -> navigateMatch(forward = false)
            else -> Unit
        }
    }

    private fun toggleSearch() {
        val current = _state.value
        if (current.isSearchVisible) {
            _state.value = current.copy(
                isSearchVisible = false,
                searchQuery = "",
                searchMatchCount = 0,
                currentMatchIndex = 0,
            )
        } else {
            _state.value = current.copy(isSearchVisible = true)
        }
    }

    private fun updateSearchQuery(query: String) {
        val matchCount = if (query.isBlank()) {
            0
        } else {
            countMatches(query)
        }
        _state.value = _state.value.copy(
            searchQuery = query,
            searchMatchCount = matchCount,
            currentMatchIndex = if (matchCount > 0) 0 else 0,
        )
    }

    private fun countMatches(query: String): Int {
        val lowerQuery = query.lowercase()
        return _state.value.materials.sumOf { material ->
            val text = material.viewContent?.let { stripHtmlTags(it) }.orEmpty()
            if (text.isBlank()) return@sumOf 0
            var count = 0
            var startIndex = 0
            val lowerText = text.lowercase()
            while (true) {
                val index = lowerText.indexOf(lowerQuery, startIndex)
                if (index < 0) break
                count++
                startIndex = index + 1
            }
            count
        }
    }

    private fun navigateMatch(forward: Boolean) {
        val current = _state.value
        if (current.searchMatchCount <= 0) return
        val newIndex = if (forward) {
            (current.currentMatchIndex + 1) % current.searchMatchCount
        } else {
            (current.currentMatchIndex - 1 + current.searchMatchCount) %
                current.searchMatchCount
        }
        _state.value = current.copy(currentMatchIndex = newIndex)
    }

    companion object {
        private val UNSAFE_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")
    }

    private suspend fun refreshTaskDetails(taskId: Int) {
        val details = taskRepository.fetchTaskDetails(taskId)
        if (details != null) {
            val updated = _state.value.taskDetails.toMutableMap()
            updated[taskId] = details
            _state.value = _state.value.copy(taskDetails = updated)
        }
        loadTaskEventsAndComments(taskId)
    }
}
