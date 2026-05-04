package io.github.kroune.cumobile.presentation.courses.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.formatDeadlineShort
import io.github.kroune.cumobile.presentation.common.model.CourseOverviewUi
import io.github.kroune.cumobile.presentation.common.model.CourseThemeUi
import io.github.kroune.cumobile.presentation.common.model.LongreadSummaryUi
import io.github.kroune.cumobile.presentation.common.model.ThemeExerciseUi
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.courses.detail.CourseDetailComponent
import kotlinx.collections.immutable.persistentListOf

private val previewThemes = persistentListOf(
    CourseThemeUi(
        id = "1",
        name = "Введение в алгоритмы",
        order = 1,
        state = "published",
        longreads = persistentListOf(
            LongreadSummaryUi(
                id = "1",
                name = "Теория сложности",
                type = "markdown",
                state = "published",
                exercises = persistentListOf(),
            ),
            LongreadSummaryUi(
                id = "2",
                name = "Практика: сортировки",
                type = "coding",
                state = "published",
                exercises = persistentListOf(
                    ThemeExerciseUi(
                        id = "1",
                        name = "ДЗ: Быстрая сортировка",
                        maxScore = 10,
                        deadlineFormatted = formatDeadlineShort("2026-04-01T23:59:00"),
                        activityName = null,
                    ),
                ),
            ),
        ),
    ),
    CourseThemeUi(
        id = "2",
        name = "Графы и деревья",
        order = 2,
        state = "published",
        longreads = persistentListOf(
            LongreadSummaryUi(
                id = "3",
                name = "BFS и DFS",
                type = "markdown",
                state = "published",
                exercises = persistentListOf(),
            ),
            LongreadSummaryUi(
                id = "4",
                name = "Задачи на графы",
                type = "coding",
                state = "published",
                exercises = persistentListOf(
                    ThemeExerciseUi(
                        id = "2",
                        name = "ДЗ: Кратчайшие пути",
                        maxScore = 10,
                        deadlineFormatted = formatDeadlineShort("2026-04-10T23:59:00"),
                        activityName = null,
                    ),
                    ThemeExerciseUi(
                        id = "3",
                        name = "ДЗ: Минимальное остовное дерево",
                        maxScore = 10,
                        deadlineFormatted = null,
                        activityName = null,
                    ),
                ),
            ),
        ),
    ),
    CourseThemeUi(
        id = "3",
        name = "Динамическое программирование",
        order = 3,
        state = "published",
        longreads = persistentListOf(),
    ),
)

private val previewCourseDetailState =
    CourseDetailComponent.State(
        courseId = "1",
        isLoading = false,
        overview = CourseOverviewUi(
            id = "1",
            name = "Алгоритмы и структуры данных",
            isArchived = false,
            themes = previewThemes,
        ),
        filteredThemes = previewThemes,
    )

@Preview
@Composable
private fun PreviewCourseDetailDark() {
    CuMobileTheme(darkTheme = true) {
        CourseDetailScreenContent(
            state = previewCourseDetailState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCourseDetailLight() {
    CuMobileTheme(darkTheme = false) {
        CourseDetailScreenContent(
            state = previewCourseDetailState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCourseDetailExpandedDark() {
    CuMobileTheme(darkTheme = true) {
        CourseDetailScreenContent(
            state = previewCourseDetailState.copy(expandedThemeIds = setOf("1")),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCourseDetailLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        CourseDetailScreenContent(
            state = CourseDetailComponent.State(isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCourseDetailErrorDark() {
    CuMobileTheme(darkTheme = true) {
        CourseDetailScreenContent(
            state = CourseDetailComponent.State(
                isLoading = false,
                error = "Не удалось загрузить курс",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCourseDetailErrorLight() {
    CuMobileTheme(darkTheme = false) {
        CourseDetailScreenContent(
            state = CourseDetailComponent.State(
                isLoading = false,
                error = "Не удалось загрузить курс",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
