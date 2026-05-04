package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.ActivitySummaryUi
import io.github.kroune.cumobile.presentation.common.model.ExerciseWithScoreUi
import io.github.kroune.cumobile.presentation.common.model.StatusStyle
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.performance.CoursePerformanceComponent
import io.github.kroune.cumobile.presentation.performance.PerformanceData
import kotlinx.collections.immutable.persistentListOf

private val previewPerformanceExercises =
    persistentListOf(
        ExerciseWithScoreUi(
            exerciseId = "1",
            exerciseName = "ДЗ: Быстрая сортировка",
            exerciseType = "",
            themeName = "Сортировки",
            activityName = "Домашнее задание",
            scoreValue = 8.0,
            maxScore = 10,
            scoreBadgeText = "8 / 10",
            statusLabel = "Проверено",
            statusStyle = StatusStyle.Evaluated,
        ),
        ExerciseWithScoreUi(
            exerciseId = "2",
            exerciseName = "Лабораторная: Хеш-таблицы",
            exerciseType = "",
            themeName = "Хеширование",
            activityName = "Лабораторная",
            scoreValue = 5.0,
            maxScore = 10,
            scoreBadgeText = "5 / 10",
            statusLabel = "Проверено",
            statusStyle = StatusStyle.Evaluated,
        ),
        ExerciseWithScoreUi(
            exerciseId = "3",
            exerciseName = "Контрольная: Графы",
            exerciseType = "",
            themeName = "Графы",
            activityName = "Домашнее задание",
            scoreValue = 0.0,
            maxScore = 10,
            scoreBadgeText = "0 / 10",
            statusLabel = "none",
            statusStyle = StatusStyle.Backlog,
        ),
    )

private val previewActivitySummaries = persistentListOf(
    ActivitySummaryUi(
        activityId = "1",
        activityName = "Домашнее задание",
        count = 5,
        averageScore = 8.0,
        averageScoreFormatted = "8.0",
        weight = 0.4,
        weightFormatted = "0.4",
        totalContribution = 3.2,
        totalContributionFormatted = "3.2",
    ),
    ActivitySummaryUi(
        activityId = "2",
        activityName = "Лабораторная",
        count = 3,
        averageScore = 5.0,
        averageScoreFormatted = "5.0",
        weight = 0.3,
        weightFormatted = "0.3",
        totalContribution = 1.5,
        totalContributionFormatted = "1.5",
    ),
)

private val previewPerformanceState =
    CoursePerformanceComponent.State(
        courseId = "1",
        courseName = "Алгоритмы и структуры данных",
        totalGrade = 7,
        totalGradeFormatted = "7",
        totalGradeDescription = "Хорошо",
        content = ContentState.Success(
            PerformanceData(
                exercises = previewPerformanceExercises,
                activitySummaries = previewActivitySummaries,
            ),
        ),
        exercises = previewPerformanceExercises,
        activitySummaries = previewActivitySummaries,
        isContentLoading = false,
        activityNames = persistentListOf("Домашнее задание", "Лабораторная"),
        filteredExercises = previewPerformanceExercises,
        totalContribution = previewActivitySummaries.sumOf { it.totalContribution },
        totalContributionFormatted = "4.7",
    )

@Preview
@Composable
private fun PreviewPerformanceScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1"),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1"),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceScoresDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceScoresLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceTabDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState.copy(selectedTab = 1),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1"),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceErrorDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(
                courseId = "1",
                content = ContentState.Error("Не удалось загрузить успеваемость"),
                isContentLoading = false,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceErrorLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(
                courseId = "1",
                content = ContentState.Error("Не удалось загрузить успеваемость"),
                isContentLoading = false,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
