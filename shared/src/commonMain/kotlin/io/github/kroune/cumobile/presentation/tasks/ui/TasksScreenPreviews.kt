package io.github.kroune.cumobile.presentation.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskCourse
import io.github.kroune.cumobile.data.model.TaskExercise
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock
import io.github.kroune.cumobile.presentation.tasks.TasksComponent
import io.github.kroune.cumobile.presentation.tasks.buildTasksContent
import kotlinx.collections.immutable.persistentListOf

private fun previewState(
    tasks: List<StudentTask>,
    segment: Int = 0,
    statusFilter: String? = null,
    courseFilter: String? = null,
    searchQuery: String = "",
) =
    TasksComponent.State(
        content = ContentState.Success(
            buildTasksContent(
                allTasks = tasks,
                segment = segment,
                statusFilter = statusFilter,
                courseFilter = courseFilter,
                searchQuery = searchQuery,
            ),
        ),
        segment = segment,
        statusFilter = statusFilter,
        courseFilter = courseFilter,
        searchQuery = searchQuery,
    )

private val previewActiveTasks = persistentListOf(
    StudentTask(
        id = "1",
        state = TaskState.InProgress,
        exercise = TaskExercise(name = "ДЗ: Деревья и графы", deadline = "2026-04-01T23:59:00"),
        course = TaskCourse(id = "1", name = "Алгоритмы"),
    ),
    StudentTask(
        id = "2",
        state = TaskState.Backlog,
        exercise = TaskExercise(name = "Лабораторная 3", deadline = "2026-04-05T23:59:00"),
        course = TaskCourse(id = "2", name = "Линейная алгебра"),
    ),
    StudentTask(
        id = "3",
        state = TaskState.Review,
        exercise = TaskExercise(name = "Эссе по менеджменту"),
        course = TaskCourse(id = "3", name = "Менеджмент"),
    ),
)

private val previewArchiveTasks = persistentListOf(
    StudentTask(
        id = "10",
        state = TaskState.Evaluated,
        score = 8.0,
        exercise = TaskExercise(name = "ДЗ: Сортировки"),
        course = TaskCourse(id = "1", name = "Алгоритмы"),
    ),
    StudentTask(
        id = "11",
        state = TaskState.Failed,
        score = 2.0,
        exercise = TaskExercise(name = "Контрольная: Матрицы"),
        course = TaskCourse(id = "2", name = "Линейная алгебра"),
    ),
)

@Preview
@Composable
private fun PreviewTasksScreenDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            TasksScreenContent(state = previewState(previewActiveTasks), onIntent = {})
        }
    }
}

@Preview
@Composable
private fun PreviewTasksScreenLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            TasksScreenContent(state = previewState(previewActiveTasks), onIntent = {})
        }
    }
}

@Preview
@Composable
private fun PreviewTasksLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = TasksComponent.State(content = ContentState.Loading),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksErrorDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = TasksComponent.State(
                content = ContentState.Error("Не удалось загрузить задания"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksErrorLight() {
    CuMobileTheme(darkTheme = false) {
        TasksScreenContent(
            state = TasksComponent.State(
                content = ContentState.Error("Не удалось загрузить задания"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksEmptyFiltersDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            TasksScreenContent(
                state = previewState(
                    tasks = previewActiveTasks,
                    searchQuery = "несуществующий запрос",
                ),
                onIntent = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTasksArchiveDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = previewState(tasks = previewArchiveTasks, segment = 1),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksWithFiltersDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            TasksScreenContent(
                state = previewState(
                    tasks = previewActiveTasks,
                    statusFilter = TaskState.InProgress,
                    courseFilter = "1",
                ),
                onIntent = {},
            )
        }
    }
}
