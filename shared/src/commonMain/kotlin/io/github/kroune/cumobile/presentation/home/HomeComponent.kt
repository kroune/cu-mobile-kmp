package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.isLoading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * MVI component for the Home tab ("Главная").
 *
 * Displays three sections:
 * - **Deadlines**: horizontally scrollable cards of active tasks.
 * - **Schedule**: weekly class schedule with week navigation.
 * - **Courses**: grid of active (non-archived) courses.
 */
interface HomeComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect
    }

    data class State(
        val tasks: ContentState<ImmutableList<StudentTask>> = ContentState.Loading,
        val courses: ContentState<ImmutableList<Course>> = ContentState.Loading,
        val schedule: ContentState<ImmutableList<ClassData>> = ContentState.Loading,
        val selectedDate: LocalDate = PLACEHOLDER_DATE,
        val weekStart: LocalDate = PLACEHOLDER_DATE,
    ) {
        /** Whether any important content is still loading. */
        val isContentLoading: Boolean
            get() = tasks.isLoading && courses.isLoading

        /**
         * Active tasks suitable for the deadlines section.
         *
         * Filters out tasks from archived courses and keeps only actionable states.
         * Sorted by deadline ascending (tasks without deadlines go to the end).
         */
        val deadlineTasks: ImmutableList<StudentTask>
            get() = tasks.dataOrNull
                .orEmpty()
                .filter { !it.course.isArchived }
                .filter { it.state in ACTIVE_TASK_STATES }
                .sortedBy { it.exercise.deadline ?: "9999-12-31" }
                .toImmutableList()

        /** Active (non-archived) courses. */
        val activeCourses: ImmutableList<Course>
            get() = courses.dataOrNull
                .orEmpty()
                .filter { !it.isArchived }
                .toImmutableList()
    }

    sealed interface Intent {
        /** Navigate to a task's longread page. */
        data class OpenTask(
            val task: StudentTask,
        ) : Intent

        /** Navigate to a course detail page. */
        data class OpenCourse(
            val courseId: String,
        ) : Intent

        /** Pull-to-refresh triggered. */
        data object Refresh : Intent

        /** Navigate to previous week in schedule. */
        data object PreviousWeek : Intent

        /** Navigate to next week in schedule. */
        data object NextWeek : Intent

        /** Select a specific date in the week picker. */
        data class SelectDate(
            val date: LocalDate,
        ) : Intent
    }

    companion object {
        /** Placeholder date used as default before real date is set from DateTimeProvider. */
        private val PLACEHOLDER_DATE = LocalDate.fromEpochDays(0)

        /** Task states shown in the deadlines section. */
        val ACTIVE_TASK_STATES = setOf(
            TaskState.Backlog,
            TaskState.InProgress,
            TaskState.Revision,
            TaskState.Rework,
        )
    }
}
