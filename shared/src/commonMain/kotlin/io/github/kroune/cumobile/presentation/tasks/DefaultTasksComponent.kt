package io.github.kroune.cumobile.presentation.tasks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Default implementation of [TasksComponent].
 *
 * Loads tasks from the API, applies segment/status/course/search
 * filters locally, and sorts by deadline. Delegates task-open
 * navigation to the parent component via [onOpenTask].
 */
class DefaultTasksComponent(
    componentContext: ComponentContext,
    private val taskRepository: TaskRepository,
    private val onOpenTask: (StudentTask) -> Unit,
) : TasksComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(TasksComponent.State())
    override val state: Value<TasksComponent.State> = _state

    override fun onIntent(intent: TasksComponent.Intent) {
        when (intent) {
            is TasksComponent.Intent.SelectSegment ->
                _state.value = _state.value.copy(
                    segment = intent.index,
                    statusFilter = null,
                )
            is TasksComponent.Intent.FilterByStatus ->
                _state.value = _state.value.copy(
                    statusFilter = intent.status,
                )
            is TasksComponent.Intent.FilterByCourse ->
                _state.value = _state.value.copy(
                    courseFilter = intent.courseId,
                )
            is TasksComponent.Intent.Search ->
                _state.value = _state.value.copy(
                    searchQuery = intent.query,
                )
            is TasksComponent.Intent.OpenTask ->
                onOpenTask(intent.task)
            TasksComponent.Intent.Refresh ->
                loadTasks()
        }
    }

    init {
        loadTasks()
    }

    private fun loadTasks() {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
            )
            val result = taskRepository.fetchTasks(AllApiStates)
            if (result != null) {
                _state.value = _state.value.copy(
                    allTasks = result,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить задания",
                )
            }
        }
    }
}

/**
 * Returns the filtered and sorted task list based on the current state.
 *
 * Filtering order:
 * 1. Segment filter (Active / Archive).
 * 2. Status filter (specific normalized state).
 * 3. Course filter (specific course ID).
 * 4. Search query (exercise name contains query, case-insensitive).
 *
 * Sorting: tasks with evaluated/failed/rejected/review states go
 * to the bottom; remaining are sorted by deadline ascending
 * (null deadlines last).
 */
internal fun filteredTasks(state: TasksComponent.State): List<StudentTask> {
    val segmentStates = if (state.segment == 0) {
        ActiveStates
    } else {
        ArchiveStates
    }
    return state.allTasks
        .filter { task ->
            normalizeTaskState(effectiveTaskState(task)) in segmentStates
        }.filter { task ->
            val status = state.statusFilter ?: return@filter true
            normalizeTaskState(effectiveTaskState(task)) == status
        }.filter { task ->
            val courseId = state.courseFilter ?: return@filter true
            task.course.id == courseId
        }.filter { task ->
            val query = state.searchQuery
            query.isEmpty() ||
                task.exercise.name.contains(query, ignoreCase = true)
        }.sortedWith(taskComparator())
}

/**
 * Comparator matching the Flutter reference sorting:
 * evaluated/failed/rejected/review tasks go to the bottom;
 * remaining sorted by deadline ascending (null deadlines last).
 */
private val BottomStates = setOf(
    TaskState.Evaluated,
    TaskState.Failed,
    TaskState.Rejected,
    TaskState.Review,
)

private fun taskComparator(): Comparator<StudentTask> =
    Comparator { a, b ->
        val aBottom = normalizeTaskState(a.state) in BottomStates
        val bBottom = normalizeTaskState(b.state) in BottomStates
        if (aBottom != bBottom) {
            return@Comparator if (aBottom) 1 else -1
        }
        val deadlineA = a.deadline ?: a.exercise.deadline
        val deadlineB = b.deadline ?: b.exercise.deadline
        when {
            deadlineA == null && deadlineB == null -> 0
            deadlineA == null -> 1
            deadlineB == null -> -1
            else -> deadlineA.compareTo(deadlineB)
        }
    }

/**
 * Returns the distinct course names available for the course filter
 * dropdown, sorted alphabetically.
 */
internal fun availableCourses(tasks: List<StudentTask>): List<Pair<Int, String>> =
    tasks
        .map { it.course.id to it.course.name }
        .distinctBy { it.first }
        .sortedBy { it.second }

/**
 * Returns the distinct status labels available for the status filter
 * dropdown in the current segment.
 */
internal fun availableStatuses(
    tasks: List<StudentTask>,
    segment: Int,
): List<String> {
    val segmentStates = if (segment == 0) {
        ActiveStates
    } else {
        ArchiveStates
    }
    return tasks
        .map { normalizeTaskState(effectiveTaskState(it)) }
        .filter { it in segmentStates }
        .distinct()
        .sorted()
}
