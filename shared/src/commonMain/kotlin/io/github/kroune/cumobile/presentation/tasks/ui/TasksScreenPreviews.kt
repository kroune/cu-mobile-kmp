package io.github.kroune.cumobile.presentation.tasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.mappers.toApiValue
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.parseDeadlineInstant
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock
import io.github.kroune.cumobile.presentation.tasks.TasksComponent
import io.github.kroune.cumobile.presentation.tasks.buildTasksContent
import kotlinx.collections.immutable.persistentListOf

private fun previewState(
    tasks: List<TaskDomain>,
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
                now = previewClock.now(),
            ),
        ),
        segment = segment,
        statusFilter = statusFilter,
        courseFilter = courseFilter,
        searchQuery = searchQuery,
    )

private fun previewTaskDomain(
    id: String = "",
    status: TaskStatus = TaskStatus.Backlog,
    exerciseName: String = "",
    deadline: String? = null,
    courseId: String = "",
    courseName: String = "",
    score: Double? = null,
) =
    TaskDomain(
        id = id,
        status = status,
        score = score,
        extraScore = null,
        deadline = null,
        submitAt = null,
        startedAt = null,
        exerciseId = "",
        exerciseName = exerciseName,
        exerciseType = "",
        exerciseMaxScore = 0,
        exerciseDeadline = parseDeadlineInstant(deadline),
        activityName = null,
        activityWeight = null,
        courseId = courseId,
        courseName = courseName,
        courseIsArchived = status.isArchived,
        themeId = "",
        themeName = "",
        longreadId = "",
        isLateDaysEnabled = false,
        lateDays = null,
    )

private val previewActiveTasks = persistentListOf(
    previewTaskDomain(
        id = "1",
        status = TaskStatus.InProgress,
        exerciseName = "ДЗ: Деревья и графы",
        deadline = "2026-04-01T23:59:00",
        courseId = "1",
        courseName = "Алгоритмы",
    ),
    previewTaskDomain(
        id = "2",
        status = TaskStatus.Backlog,
        exerciseName = "Лабораторная 3",
        deadline = "2026-04-05T23:59:00",
        courseId = "2",
        courseName = "Линейная алгебра",
    ),
    previewTaskDomain(
        id = "3",
        status = TaskStatus.Review,
        exerciseName = "Эссе по менеджменту",
        courseId = "3",
        courseName = "Менеджмент",
    ),
)

private val previewArchiveTasks = persistentListOf(
    previewTaskDomain(
        id = "10",
        status = TaskStatus.Evaluated,
        exerciseName = "ДЗ: Сортировки",
        courseId = "1",
        courseName = "Алгоритмы",
        score = 8.0,
    ),
    previewTaskDomain(
        id = "11",
        status = TaskStatus.Failed,
        exerciseName = "Контрольная: Матрицы",
        courseId = "2",
        courseName = "Линейная алгебра",
        score = 2.0,
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
                    statusFilter = TaskStatus.InProgress.toApiValue(),
                    courseFilter = "1",
                ),
                onIntent = {},
            )
        }
    }
}
