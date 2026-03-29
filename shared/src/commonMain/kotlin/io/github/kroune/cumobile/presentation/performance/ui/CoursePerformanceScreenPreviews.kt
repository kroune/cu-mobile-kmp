package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.CourseExerciseActivity
import io.github.kroune.cumobile.data.model.CourseExerciseTheme
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.data.model.TaskScoreActivity
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.performance.ActivitySummary
import io.github.kroune.cumobile.presentation.performance.CoursePerformanceComponent
import io.github.kroune.cumobile.presentation.performance.ExerciseWithScore
import io.github.kroune.cumobile.presentation.performance.PerformanceData
import kotlinx.collections.immutable.persistentListOf

private val previewPerformanceExercises =
    persistentListOf(
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "1",
                name = "ДЗ: Быстрая сортировка",
                activity = CourseExerciseActivity(id = "1", name = "Домашнее задание"),
                theme = CourseExerciseTheme(id = "1", name = "Сортировки"),
            ),
            score = TaskScore(
                id = "1",
                score = 8.0,
                maxScore = 10,
                exerciseId = "1",
                state = "evaluated",
                activity = TaskScoreActivity(
                    id = "1",
                    name = "Домашнее задание",
                    weight = 0.4,
                ),
            ),
        ),
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "2",
                name = "Лабораторная: Хеш-таблицы",
                activity = CourseExerciseActivity(id = "2", name = "Лабораторная"),
                theme = CourseExerciseTheme(id = "2", name = "Хеширование"),
            ),
            score = TaskScore(
                id = "2",
                score = 5.0,
                maxScore = 10,
                exerciseId = "2",
                state = "evaluated",
                activity = TaskScoreActivity(
                    id = "2",
                    name = "Лабораторная",
                    weight = 0.3,
                ),
            ),
        ),
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "3",
                name = "Контрольная: Графы",
                activity = CourseExerciseActivity(id = "1", name = "Домашнее задание"),
                theme = CourseExerciseTheme(id = "3", name = "Графы"),
            ),
            score = null,
        ),
    )

private val previewPerformanceState =
    CoursePerformanceComponent.State(
        courseId = "1",
        courseName = "Алгоритмы и структуры данных",
        totalGrade = 7,
        content = ContentState.Success(
            PerformanceData(
                exercises = previewPerformanceExercises,
                activitySummaries = persistentListOf(
                    ActivitySummary(
                        activityId = "1",
                        activityName = "Домашнее задание",
                        count = 5,
                        averageScore = 8.0,
                        weight = 0.4,
                    ),
                    ActivitySummary(
                        activityId = "2",
                        activityName = "Лабораторная",
                        count = 3,
                        averageScore = 5.0,
                        weight = 0.3,
                    ),
                ),
            ),
        ),
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
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
