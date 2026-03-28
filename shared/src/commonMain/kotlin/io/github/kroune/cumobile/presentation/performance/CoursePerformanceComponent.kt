package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.isLoading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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
        val content: ContentState<PerformanceData> = ContentState.Loading,
        val selectedTab: Int = 0,
        val activityFilter: String? = null,
    ) {
        /** Exercises from loaded data. */
        val exercises: ImmutableList<ExerciseWithScore>
            get() = content.dataOrNull
                ?.exercises
                .orEmpty()
                .toImmutableList()

        /** Activity summaries from loaded data. */
        val activitySummaries: ImmutableList<ActivitySummary>
            get() = content.dataOrNull
                ?.activitySummaries
                .orEmpty()
                .toImmutableList()

        /** Whether content is still loading. */
        val isContentLoading: Boolean
            get() = content.isLoading

        /** All unique activity names for the filter chips. */
        val activityNames: ImmutableList<String>
            get() = exercises
                .map { it.activityName }
                .distinct()
                .sorted()
                .toImmutableList()

        /** Exercises filtered by the selected activity. */
        val filteredExercises: ImmutableList<ExerciseWithScore>
            get() = if (activityFilter == null) {
                exercises
            } else {
                exercises.filter { it.activityName == activityFilter }.toImmutableList()
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
 * Container for loaded performance data (exercises + activity summaries).
 */
data class PerformanceData(
    val exercises: ImmutableList<ExerciseWithScore>,
    val activitySummaries: ImmutableList<ActivitySummary>,
)

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
