package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState

/**
 * MVI component for the Home tab ("Главная").
 *
 * Displays three sections:
 * - **Deadlines**: horizontally scrollable cards of active tasks.
 * - **Schedule**: daily class schedule with date navigation.
 * - **Courses**: grid of active (non-archived) courses.
 */
interface HomeComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val tasks: List<StudentTask> = emptyList(),
        val courses: List<Course> = emptyList(),
        /** Daily schedule classes for the selected date. */
        val classes: List<ClassData> = emptyList(),
        val selectedDateMillis: Long = 0,
        val isScheduleLoading: Boolean = false,
        val scheduleError: String? = null,
        val profileInitials: String = "",
        val lateDaysBalance: Int? = null,
        val isLoading: Boolean = true,
        val error: String? = null,
    ) {
        /**
         * Active tasks suitable for the deadlines section.
         *
         * Filters out tasks from archived courses and keeps only actionable states.
         * Sorted by deadline ascending (tasks without deadlines go to the end).
         */
        val deadlineTasks: List<StudentTask>
            get() = tasks
                .filter { !it.course.isArchived }
                .filter { it.state in ACTIVE_TASK_STATES }
                .sortedBy { it.exercise.deadline ?: "9999-12-31" }

        /** Active (non-archived) courses. */
        val activeCourses: List<Course>
            get() = courses.filter { !it.isArchived }
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

        /** Navigate to previous day in schedule. */
        data object PreviousDay : Intent

        /** Navigate to next day in schedule. */
        data object NextDay : Intent

        /** Navigate to today in schedule. */
        data object Today : Intent

        /** Navigate to profile screen. */
        data object OpenProfile : Intent
    }

    companion object {
        /** Task states shown in the deadlines section. */
        val ACTIVE_TASK_STATES = setOf(
            TaskState.Backlog,
            TaskState.InProgress,
            TaskState.Revision,
            TaskState.Rework,
        )
    }
}
