package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.TaskScore

/**
 * Component for the course performance screen.
 *
 * Shows two tabs:
 * - "Набранные баллы" (Scores) — exercise tiles with scores, filterable by activity.
 * - "Успеваемость" (Performance) — activity summaries with weighted averages.
 */
interface CoursePerformanceComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val courseId: String,
        val courseName: String = "",
        val totalGrade: Int = 0,
        val exercises: List<ExerciseWithScore> = emptyList(),
        val activitySummaries: List<ActivitySummary> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedTab: Int = 0,
        val activityFilter: String? = null,
    ) {
        /** All unique activity names for the filter chips. */
        val activityNames: List<String>
            get() = exercises
                .map { it.activityName }
                .distinct()
                .sorted()

        /** Exercises filtered by the selected activity. */
        val filteredExercises: List<ExerciseWithScore>
            get() = if (activityFilter == null) {
                exercises
            } else {
                exercises.filter { it.activityName == activityFilter }
            }

        /** Grand total contribution across all activities. */
        val totalContribution: Double
            get() = activitySummaries.sumOf { it.totalContribution }
    }

    sealed interface Intent {
        data object Back : Intent

        data object Refresh : Intent

        data class SelectTab(
            val index: Int,
        ) : Intent

        data class FilterByActivity(
            val activityName: String?,
        ) : Intent
    }
}

/**
 * Joins a [CourseExercise] with its optional [TaskScore].
 *
 * Matched by [CourseExercise.id] == [TaskScore.exerciseId].
 */
data class ExerciseWithScore(
    val exercise: CourseExercise,
    val score: TaskScore?,
) {
    val themeName: String
        get() = exercise.theme?.name ?: "Без темы"

    val activityName: String
        get() = exercise.activity?.name ?: "Без активности"

    val scoreValue: Double
        get() = score?.score ?: 0.0

    val maxScore: Int
        get() = score?.maxScore ?: 10

    val state: String
        get() = score?.state ?: "none"
}

/**
 * Aggregated performance summary for a single activity type.
 *
 * Computed locally from [TaskScore] data, not deserialized from API.
 */
data class ActivitySummary(
    val activityId: String,
    val activityName: String,
    val count: Int,
    val averageScore: Double,
    val weight: Double,
) {
    val totalContribution: Double
        get() = averageScore * weight
}
