package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.presentation.common.ContentState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
        val exercises: ImmutableList<ExerciseWithScore> = persistentListOf(),
        val activitySummaries: ImmutableList<ActivitySummary> = persistentListOf(),
        val isContentLoading: Boolean = true,
        val activityNames: ImmutableList<String> = persistentListOf(),
        val filteredExercises: ImmutableList<ExerciseWithScore> = persistentListOf(),
        val totalContribution: Double = 0.0,
    )

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
private const val DefaultMaxScore = 10

data class ExerciseWithScore(
    val exercise: CourseExercise,
    val score: TaskScore?,
    val themeName: String = exercise.theme?.name ?: "Без темы",
    val activityName: String = exercise.activity?.name ?: "Без активности",
    val scoreValue: Double = score?.score ?: 0.0,
    val maxScore: Int = score?.maxScore ?: DefaultMaxScore,
    val state: String = score?.state ?: "none",
)

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
    val totalContribution: Double = averageScore * weight,
)
