package io.github.kroune.cumobile.presentation.home

import androidx.compose.ui.graphics.ImageBitmap
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
        val avatarBitmap: ImageBitmap? = null,
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            return tasks == other.tasks &&
                courses == other.courses &&
                classes == other.classes &&
                selectedDateMillis == other.selectedDateMillis &&
                isScheduleLoading == other.isScheduleLoading &&
                scheduleError == other.scheduleError &&
                profileInitials == other.profileInitials &&
                avatarBitmap === other.avatarBitmap &&
                lateDaysBalance == other.lateDaysBalance &&
                isLoading == other.isLoading &&
                error == other.error
        }

        override fun hashCode(): Int {
            var result = tasks.hashCode()
            result = 31 * result + courses.hashCode()
            result = 31 * result + classes.hashCode()
            result = 31 * result + selectedDateMillis.hashCode()
            result = 31 * result + isScheduleLoading.hashCode()
            result = 31 * result + (scheduleError?.hashCode() ?: 0)
            result = 31 * result + profileInitials.hashCode()
            result = 31 * result + (avatarBitmap?.hashCode() ?: 0)
            result = 31 * result + (lateDaysBalance?.hashCode() ?: 0)
            result = 31 * result + isLoading.hashCode()
            result = 31 * result + (error?.hashCode() ?: 0)
            return result
        }
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
