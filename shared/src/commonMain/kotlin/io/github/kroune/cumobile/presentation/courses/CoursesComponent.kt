package io.github.kroune.cumobile.presentation.courses

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceCourse

/**
 * MVI component for the Courses tab ("Обучение").
 *
 * Three segments:
 * - 0: Курсы — list of active/archived courses with category info.
 * - 1: Ведомость — grade sheet (performance per course).
 * - 2: Зачетка — record book (gradebook by semester).
 */
interface CoursesComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        /** All courses loaded from the API. */
        val courses: List<Course> = emptyList(),
        /** Performance data for grade sheet segment. */
        val performanceCourses: List<StudentPerformanceCourse> = emptyList(),
        /** Gradebook data for record book segment. */
        val gradebook: GradebookResponse? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Currently selected segment: 0 = Courses, 1 = Grade Sheet, 2 = Record Book. */
        val segment: Int = 0,
        /** Whether to show archived courses in the courses segment. */
        val showArchived: Boolean = false,
    )

    sealed interface Intent {
        /** Switch between Courses (0), Grade Sheet (1), Record Book (2). */
        data class SelectSegment(
            val index: Int,
        ) : Intent

        /** Toggle display of archived courses. */
        data object ToggleArchived : Intent

        /** Open course detail. */
        data class OpenCourse(
            val courseId: Int,
        ) : Intent

        /** Open course performance page. */
        data class OpenCoursePerformance(
            val courseId: Int,
            val courseName: String,
            val totalGrade: Int,
        ) : Intent

        /** Refresh all data. */
        data object Refresh : Intent
    }
}

/** Returns active (non-archived) courses sorted alphabetically. */
internal fun activeCourses(courses: List<Course>): List<Course> =
    courses
        .filter { !it.isArchived }
        .sortedBy { it.name }

/** Returns archived courses sorted alphabetically. */
internal fun archivedCourses(courses: List<Course>): List<Course> =
    courses
        .filter { it.isArchived }
        .sortedBy { it.name }
