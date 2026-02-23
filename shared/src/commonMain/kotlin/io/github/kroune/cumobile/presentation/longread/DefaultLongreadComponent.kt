package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    private val contentRepository: ContentRepository,
    private val taskRepository: TaskRepository,
    private val onBack: () -> Unit,
    private val onDownloadReady: (url: String, filename: String) -> Unit,
) : LongreadComponent,
    ComponentContext by componentContext {
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
            LongreadComponent.Intent.StartTask -> startTask()
            LongreadComponent.Intent.SubmitSolution -> submitSolution()
            LongreadComponent.Intent.CreateComment -> createComment()
            LongreadComponent.Intent.ProlongLateDays -> prolongLateDays()
            LongreadComponent.Intent.CancelLateDays -> cancelLateDays()
            is LongreadComponent.Intent.DownloadFile ->
                downloadFile(intent.filename, intent.version)
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
            if (success) refreshTaskDetails(taskId)
        }
    }

    private fun submitSolution() {
        val taskId = _state.value.activeTaskId ?: return
        val url = _state.value.solutionUrl.takeIf { it.isNotBlank() }
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val success = taskRepository.submitTask(taskId, url)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                _state.value = _state.value.copy(solutionUrl = "")
                refreshTaskDetails(taskId)
            }
        }
    }

    private fun createComment() {
        val taskId = _state.value.activeTaskId ?: return
        val text = _state.value.commentText.trim()
        if (text.isEmpty()) return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            taskRepository.createComment(taskId, text)
            _state.value = _state.value.copy(
                isSubmitting = false,
                commentText = "",
            )
            val comments = taskRepository.fetchTaskComments(taskId)
            _state.value = _state.value.copy(
                taskComments = comments.orEmpty(),
            )
        }
    }

    private fun prolongLateDays() {
        val taskId = _state.value.activeTaskId ?: return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            taskRepository.prolongLateDays(taskId, 1)
            _state.value = _state.value.copy(isSubmitting = false)
            refreshTaskDetails(taskId)
        }
    }

    private fun cancelLateDays() {
        val taskId = _state.value.activeTaskId ?: return
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            taskRepository.cancelLateDays(taskId)
            _state.value = _state.value.copy(isSubmitting = false)
            refreshTaskDetails(taskId)
        }
    }

    private fun downloadFile(
        filename: String,
        version: String,
    ) {
        scope.launch {
            val url = contentRepository.getDownloadLink(filename, version)
            if (url != null) {
                val localFilename = buildLocalFilename(filename, version)
                onDownloadReady(url, localFilename)
            }
        }
    }

    /**
     * Builds a local filename from the content filename and version.
     *
     * Format: `{name}_{version}.{ext}` with unsafe characters replaced.
     */
    private fun buildLocalFilename(
        filename: String,
        version: String,
    ): String {
        val dotIndex = filename.lastIndexOf('.')
        val baseName = if (dotIndex > 0) filename.substring(0, dotIndex) else filename
        val extension = if (dotIndex > 0) filename.substring(dotIndex) else ""
        val safeName = baseName.replace(UNSAFE_CHARS_REGEX, "_")
        return "${safeName}_$version$extension"
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
