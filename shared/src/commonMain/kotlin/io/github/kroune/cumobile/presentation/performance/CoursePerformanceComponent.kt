package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.ActivitySummaryUi
import io.github.kroune.cumobile.presentation.common.model.ExerciseWithScoreUi
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
        val totalGradeFormatted: String = "0",
        val totalGradeDescription: String = "",
        val content: ContentState<PerformanceData> = ContentState.Loading,
        val selectedTab: Int = 0,
        val activityFilter: String? = null,
        val exercises: ImmutableList<ExerciseWithScoreUi> = persistentListOf(),
        val activitySummaries: ImmutableList<ActivitySummaryUi> = persistentListOf(),
        val isContentLoading: Boolean = true,
        val activityNames: ImmutableList<String> = persistentListOf(),
        val filteredExercises: ImmutableList<ExerciseWithScoreUi> = persistentListOf(),
        val totalContribution: Double = 0.0,
        val totalContributionFormatted: String = "0",
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
    val exercises: ImmutableList<ExerciseWithScoreUi>,
    val activitySummaries: ImmutableList<ActivitySummaryUi>,
)
