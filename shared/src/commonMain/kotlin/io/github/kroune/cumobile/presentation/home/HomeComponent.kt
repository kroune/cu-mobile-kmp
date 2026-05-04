package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.ClassDataUi
import io.github.kroune.cumobile.presentation.common.model.CourseUi
import io.github.kroune.cumobile.presentation.common.model.TaskUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
        val tasks: ContentState<ImmutableList<TaskUi>> = ContentState.Loading,
        val courses: ContentState<ImmutableList<CourseUi>> = ContentState.Loading,
        val schedule: ContentState<ImmutableList<ClassDataUi>> = ContentState.Loading,
        val selectedDate: LocalDate = PLACEHOLDER_DATE,
        val weekStart: LocalDate = PLACEHOLDER_DATE,
        /** Pre-formatted label for the current week range (e.g. "1 - 7 февраля"). */
        val weekRangeLabel: String = "",
        /** Active tasks suitable for the deadlines section, pre-computed on dispatchers.default. */
        val deadlineTasks: ImmutableList<TaskUi> = persistentListOf(),
        /** Active (non-archived) courses, pre-computed on dispatchers.default. */
        val activeCourses: ImmutableList<CourseUi> = persistentListOf(),
    )

    sealed interface Intent {
        /** Navigate to a task's longread page. */
        data class OpenTask(
            val taskId: String,
            val courseId: String,
            val themeId: String,
            val longreadId: String,
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
    }
}
