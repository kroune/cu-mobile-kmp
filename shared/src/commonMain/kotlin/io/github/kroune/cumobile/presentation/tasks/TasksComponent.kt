package io.github.kroune.cumobile.presentation.tasks

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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

    /**
     * Observable state exposed to the UI. Raw user-controlled fields
     * (segment, filters, searchQuery) sit alongside a [content] value
     * that wraps the filtered/sorted/aggregated data in [ContentState].
     *
     * Derivation runs off the Main thread; see [DefaultTasksComponent].
     */
    data class State(
        val content: ContentState<Content> = ContentState.Loading,
        /** Currently selected segment: 0 = Active, 1 = Archive. */
        val segment: Int = 0,
        /** Status filter (null = all statuses in current segment). */
        val statusFilter: String? = null,
        /** Course filter (null = all courses). */
        val courseFilter: String? = null,
        /** Search query (matches against exercise name). */
        val searchQuery: String = "",
    )

    /**
     * Derived view of the task list: filtered/sorted per segment,
     * plus the aggregates the UI needs (counts, available filter values).
     *
     * Produced once per raw-state mutation on [kotlinx.coroutines.Dispatchers.Default].
     */
    data class Content(
        val activeFilteredTasks: ImmutableList<StudentTask> = persistentListOf(),
        val archiveFilteredTasks: ImmutableList<StudentTask> = persistentListOf(),
        val activeCount: Int = 0,
        val archiveCount: Int = 0,
        val availableCourses: ImmutableList<Pair<String, String>> = persistentListOf(),
        val availableStatuses: ImmutableList<String> = persistentListOf(),
    )

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
