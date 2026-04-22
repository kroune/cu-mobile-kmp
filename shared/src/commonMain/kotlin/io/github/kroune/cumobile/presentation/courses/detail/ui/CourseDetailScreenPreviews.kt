package io.github.kroune.cumobile.presentation.courses.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.model.CourseTheme
import io.github.kroune.cumobile.data.model.Longread
import io.github.kroune.cumobile.data.model.ThemeExercise
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.courses.detail.CourseDetailComponent

private val previewCourseDetailState =
    CourseDetailComponent.State(
        courseId = "1",
        isLoading = false,
        overview = CourseOverview(
            id = "1",
            name = "Алгоритмы и структуры данных",
            themes = listOf(
                CourseTheme(
                    id = "1",
                    name = "Введение в алгоритмы",
                    longreads = listOf(
                        Longread(id = "1", name = "Теория сложности", type = "markdown"),
                        Longread(
                            id = "2",
                            name = "Практика: сортировки",
                            type = "coding",
                            exercises = listOf(
                                ThemeExercise(
                                    id = "1",
                                    name = "ДЗ: Быстрая сортировка",
                                    deadline = "2026-04-01T23:59:00",
                                ),
                            ),
                        ),
                    ),
                ),
                CourseTheme(
                    id = "2",
                    name = "Графы и деревья",
                    longreads = listOf(
                        Longread(id = "3", name = "BFS и DFS", type = "markdown"),
                        Longread(
                            id = "4",
                            name = "Задачи на графы",
                            type = "coding",
                            exercises = listOf(
                                ThemeExercise(
                                    id = "2",
                                    name = "ДЗ: Кратчайшие пути",
                                    deadline = "2026-04-10T23:59:00",
                                ),
                                ThemeExercise(
                                    id = "3",
                                    name = "ДЗ: Минимальное остовное дерево",
                                ),
                            ),
                        ),
                    ),
                ),
                CourseTheme(id = "3", name = "Динамическое программирование"),
            ),
        ),
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
