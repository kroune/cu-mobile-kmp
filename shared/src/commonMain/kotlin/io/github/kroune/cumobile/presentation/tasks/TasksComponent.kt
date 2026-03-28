package io.github.kroune.cumobile.presentation.tasks

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * MVI component for the Tasks tab ("Задания").
 *
 * Displays tasks in Active/Archive segments with status and course
 * filters, search, and deadline-based sorting. Tapping a task
 * navigates to the course overview / longread detail (via parent).
 *
 * Active states: backlog, inProgress, hasSolution, revision, rework, review.
 * Archive states: evaluated, failed, rejected.
 */
interface TasksComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        /** All tasks loaded from the API (unfiltered). */
        val allTasks: ImmutableList<StudentTask> = persistentListOf(),
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Currently selected segment: 0 = Active, 1 = Archive. */
        val segment: Int = 0,
        /** Status filter (null = all statuses in current segment). */
        val statusFilter: String? = null,
        /** Course filter (null = all courses). */
        val courseFilter: String? = null,
        /** Search query (matches against exercise name). */
        val searchQuery: String = "",
    ) {
        val filteredTasks: ImmutableList<StudentTask>
            get() {
                val segmentFilter = if (segment == 0) ActiveStates else ArchiveStates
                return allTasks
                    .filter { task ->
                        val effectiveState = normalizeTaskState(effectiveTaskState(task))
                        effectiveState in segmentFilter &&
                            (statusFilter == null || effectiveState == statusFilter) &&
                            (courseFilter == null || task.course.id == courseFilter) &&
                            (searchQuery.isEmpty() || task.exercise.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(taskComparator())
                    .toImmutableList()
            }

        val activeCount: Int
            get() = allTasks.count {
                normalizeTaskState(effectiveTaskState(it)) in ActiveStates
            }

        val archiveCount: Int
            get() = allTasks.count {
                normalizeTaskState(effectiveTaskState(it)) in ArchiveStates
            }

        val availableCourses: ImmutableList<Pair<String, String>>
            get() = allTasks
                .map { it.course.id to it.course.name }
                .distinctBy { it.first }
                .sortedBy { it.second }
                .toImmutableList()

        val availableStatuses: ImmutableList<String>
            get() {
                val presentStatuses = allTasks
                    .map { normalizeTaskState(effectiveTaskState(it)) }
                    .toSet()
                val segmentFilter = (if (segment == 0) ActiveStates else ArchiveStates)
                return segmentFilter
                    .filter { it in presentStatuses }
                    .toImmutableList()
            }
    }

    sealed interface Intent {
        /** Switch between Active (0) and Archive (1) segments. */
        data class SelectSegment(
            val index: Int,
        ) : Intent

        /** Filter tasks by a specific normalized state, or null for all. */
        data class FilterByStatus(
            val status: String?,
        ) : Intent

        /** Filter tasks by a specific course ID, or null for all. */
        data class FilterByCourse(
            val courseId: String?,
        ) : Intent

        /** Update the search query. */
        data class Search(
            val query: String,
        ) : Intent

        /** Open a task (navigate to longread/course detail). */
        data class OpenTask(
            val task: StudentTask,
        ) : Intent

        /** Refresh the task list from the API. */
        data object Refresh : Intent
    }
}

/**
 * Active task states (shown in the "Active" segment).
 */
internal val ActiveStates = setOf(
    TaskState.Backlog,
    TaskState.InProgress,
    TaskState.HasSolution,
    TaskState.Revision,
    TaskState.Rework,
    TaskState.Review,
)

/**
 * Archive task states (shown in the "Archive" segment).
 */
internal val ArchiveStates = setOf(
    TaskState.Evaluated,
    TaskState.Failed,
    TaskState.Rejected,
)

/**
 * All states requested from the API.
 */
internal val AllApiStates = listOf(
    TaskState.InProgress,
    TaskState.Review,
    TaskState.Backlog,
    TaskState.Failed,
    TaskState.Evaluated,
)

/**
 * Normalizes task state for display.
 *
 * Maps "rework" -> "revision" and "rejected" -> "failed" to match
 * the Flutter reference UI grouping.
 */
internal fun normalizeTaskState(state: String): String =
    when (state) {
        TaskState.Rework -> TaskState.Revision
        TaskState.Rejected -> TaskState.Failed
        else -> state
    }

/**
 * Derives a virtual "hasSolution" state when a task is inProgress
 * but already has a submitted solution.
 */
internal fun effectiveTaskState(task: StudentTask): String =
    if (task.state == TaskState.InProgress && task.submitAt != null) {
        TaskState.HasSolution
    } else {
        task.state
    }

private val BottomStates = setOf(
    TaskState.Evaluated,
    TaskState.Failed,
    TaskState.Rejected,
    TaskState.Review,
)

internal fun taskComparator(): Comparator<StudentTask> =
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
