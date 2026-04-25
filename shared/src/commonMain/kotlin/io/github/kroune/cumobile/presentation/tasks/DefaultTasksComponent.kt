package io.github.kroune.cumobile.presentation.tasks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnStart
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [TasksComponent].
 *
 * Raw state (all tasks, filters, search query, segment) is held in [raw].
 * A [TasksComponent.Content] view — filtered, sorted, and aggregated — is
 * derived on [Dispatchers.Default] whenever the raw state changes, then
 * merged back into the exposed [state] on the Main thread.
 *
 * Derivations are cancellable: a fast sequence of keystrokes on the search
 * field produces at most one completed derivation per latest value.
 */
class DefaultTasksComponent(
    componentContext: ComponentContext,
    taskRepository: Lazy<TaskRepository>,
    dispatchers: Lazy<AppDispatchers>,
    private val onOpenTask: (StudentTask) -> Unit,
) : TasksComponent,
    ComponentContext by componentContext {
    private val taskRepository by taskRepository
    private val dispatchers by dispatchers
    private val scope = componentScope()

    private val _state = MutableValue(TasksComponent.State())
    override val state: Value<TasksComponent.State> = _state

    /**
     * Raw, un-derived inputs. Kept in a single data class so every
     * mutation takes a consistent snapshot into the derivation job.
     */
    private data class RawState(
        val tasks: List<StudentTask> = emptyList(),
        val segment: Int = 0,
        val statusFilter: String? = null,
        val courseFilter: String? = null,
        val searchQuery: String = "",
    )

    private var raw = RawState()
    private var deriveJob: Job? = null

    init {
        lifecycle.doOnStart(isOneTime = true) {
            loadTasks()
        }
    }

    override fun onIntent(intent: TasksComponent.Intent) {
        when (intent) {
            is TasksComponent.Intent.SelectSegment ->
                updateRaw { copy(segment = intent.index, statusFilter = null) }
            is TasksComponent.Intent.FilterByStatus ->
                updateRaw { copy(statusFilter = intent.status) }
            is TasksComponent.Intent.FilterByCourse ->
                updateRaw { copy(courseFilter = intent.courseId) }
            is TasksComponent.Intent.Search ->
                updateRaw { copy(searchQuery = intent.query) }
            is TasksComponent.Intent.OpenTask ->
                onOpenTask(intent.task)
            TasksComponent.Intent.Refresh ->
                loadTasks()
        }
    }

    private fun loadTasks() {
        _state.value = _state.value.copy(content = ContentState.Loading)
        scope.launch {
            val result = taskRepository.fetchTasks(AllApiStates)
            if (result != null) {
                raw = raw.copy(tasks = result)
                scheduleDerive()
            } else {
                logger.warn { "Failed to load tasks" }
                deriveJob?.cancel()
                _state.value = _state.value.copy(
                    content = ContentState.Error("Не удалось загрузить задания"),
                )
            }
        }
    }

    private inline fun updateRaw(block: RawState.() -> RawState) {
        raw = raw.block()
        _state.value = _state.value.copy(
            segment = raw.segment,
            statusFilter = raw.statusFilter,
            courseFilter = raw.courseFilter,
            searchQuery = raw.searchQuery,
        )
        scheduleDerive()
    }

    /**
     * Cancels any in-flight derivation and spawns a fresh one on
     * [Dispatchers.Default] from the current [raw] snapshot. Only the
     * latest derivation reaches [state].
     */
    private fun scheduleDerive() {
        deriveJob?.cancel()
        deriveJob = scope.launch {
            val snapshot = raw
            val content = withContext(dispatchers.default) {
                buildTasksContent(
                    allTasks = snapshot.tasks,
                    segment = snapshot.segment,
                    statusFilter = snapshot.statusFilter,
                    courseFilter = snapshot.courseFilter,
                    searchQuery = snapshot.searchQuery,
                )
            }
            _state.value = _state.value.copy(content = ContentState.Success(content))
        }
    }
}
