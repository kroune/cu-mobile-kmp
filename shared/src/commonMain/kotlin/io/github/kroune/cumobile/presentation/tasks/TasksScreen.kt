package io.github.kroune.cumobile.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskCourse
import io.github.kroune.cumobile.data.model.TaskExercise
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.SegmentedControl
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix
import io.github.kroune.cumobile.presentation.common.taskStateLabel

/**
 * Tasks tab screen with segment control, filters, search, and task list.
 *
 * Matches the Flutter reference TasksTab layout:
 * 1. Segment control (Active / Archive) with task counts.
 * 2. Filter row (status dropdown, search, course dropdown, reset).
 * 3. Scrollable task list.
 * 4. Loading / error / empty states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    component: TasksComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    TasksScreenContent(
        state = state,
        onIntent = component::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasksScreenContent(
    state: TasksComponent.State,
    onIntent: (TasksComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tasks = filteredTasks(state)
    val allTasks = state.allTasks

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onIntent(TasksComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SegmentedControl(
                labels = listOf(
                    "Активные (${countBySegment(allTasks, 0)})",
                    "Архив (${countBySegment(allTasks, 1)})",
                ),
                selectedIndex = state.segment,
                onSelect = { onIntent(TasksComponent.Intent.SelectSegment(it)) },
            )

            Spacer(modifier = Modifier.height(8.dp))

            FiltersRow(
                state = state,
                allTasks = allTasks,
                onIntent = onIntent,
            )

            Spacer(modifier = Modifier.height(8.dp))

            TasksContentArea(
                state = state,
                tasks = tasks,
                onIntent = onIntent,
            )
        }
    }
}

/**
 * Content area showing loading, error, empty, or task list states.
 */
@Composable
private fun TasksContentArea(
    state: TasksComponent.State,
    tasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    onIntent: (TasksComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && tasks.isEmpty() -> LoadingContent(modifier)
        state.error != null && tasks.isEmpty() -> ErrorContent(
            error = state.error,
            onRetry = { onIntent(TasksComponent.Intent.Refresh) },
            modifier = modifier,
        )
        tasks.isEmpty() -> EmptyContent(
            text = "Нет заданий по выбранным фильтрам",
            modifier = modifier,
        )
        else -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = tasks,
                    key = { it.id },
                ) { task ->
                    TaskListItem(
                        task = task,
                        onClick = {
                            onIntent(TasksComponent.Intent.OpenTask(task))
                        },
                    )
                }
            }
        }
    }
}

/**
 * Filter row: status chips + search field + course chips + reset.
 */
@Composable
private fun FiltersRow(
    state: TasksComponent.State,
    allTasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    onIntent: (TasksComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Search field — compact height
        BasicTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(TasksComponent.Intent.Search(it)) },
            singleLine = true,
            textStyle = TextStyle(
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(AppTheme.colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .background(AppTheme.colors.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { innerTextField ->
                if (state.searchQuery.isEmpty()) {
                    Text(
                        text = "Поиск по названию...",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 14.sp,
                    )
                }
                innerTextField()
            },
        )

        StatusFilterChips(
            allTasks = allTasks,
            segment = state.segment,
            statusFilter = state.statusFilter,
            onIntent = onIntent,
        )

        CourseFilterChips(
            allTasks = allTasks,
            courseFilter = state.courseFilter,
            onIntent = onIntent,
        )
    }
}

/** Status filter chips row — selected chip moves to front. */
@Composable
private fun StatusFilterChips(
    allTasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    segment: Int,
    statusFilter: String?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val statuses = availableStatuses(allTasks, segment)
    if (statuses.isEmpty()) return
    val sorted = selectedFirst(statuses, statusFilter)
    val listState = rememberLazyListState()
    LaunchedEffect(statusFilter) {
        listState.animateScrollToItem(0)
    }
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = sorted,
            key = { it },
        ) { status ->
            FilterChip(
                label = taskStateLabel(status),
                selected = statusFilter == status,
                onClick = {
                    val newFilter = if (statusFilter == status) null else status
                    onIntent(TasksComponent.Intent.FilterByStatus(newFilter))
                },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/** Course filter chips row — selected chip moves to front. */
@Composable
private fun CourseFilterChips(
    allTasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    courseFilter: String?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val courses = availableCourses(allTasks)
    if (courses.isEmpty()) return
    val sorted = selectedFirst(courses, courseFilter) { it.first }
    val listState = rememberLazyListState()
    LaunchedEffect(courseFilter) {
        listState.animateScrollToItem(0)
    }
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = sorted,
            key = { it.first },
        ) { (courseId, courseName) ->
            FilterChip(
                label = stripEmojiPrefix(courseName),
                selected = courseFilter == courseId,
                onClick = {
                    val newFilter = if (courseFilter == courseId) null else courseId
                    onIntent(TasksComponent.Intent.FilterByCourse(newFilter))
                },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/**
 * Moves the selected item to the front, keeping relative order of the rest.
 * For simple lists where the item itself is the key (e.g., status strings).
 */
private fun <T> selectedFirst(
    items: List<T>,
    selectedKey: String?,
    keySelector: (T) -> String = { it as String },
): List<T> {
    if (selectedKey == null) return items
    val selected = items.find { keySelector(it) == selectedKey } ?: return items
    return listOf(selected) + items.filter { keySelector(it) != selectedKey }
}

/**
 * A selectable filter chip with border and tinted background.
 */
@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
                shape = shape,
            ).background(
                if (selected) {
                    AppTheme.colors.accent.copy(alpha = 0.15f)
                } else {
                    AppTheme.colors.surface
                },
            ).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

/**
 * Counts tasks belonging to the given segment.
 */
private fun countBySegment(
    tasks: List<StudentTask>,
    segment: Int,
): Int {
    val segmentStates = if (segment == 0) ActiveStates else ArchiveStates
    return tasks.count {
        normalizeTaskState(effectiveTaskState(it)) in segmentStates
    }
}

private val previewTasksState = TasksComponent.State(
    allTasks = listOf(
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
    ),
)

@Preview
@Composable
private fun PreviewTasksScreenDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(state = previewTasksState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewTasksScreenLight() {
    CuMobileTheme(darkTheme = false) {
        TasksScreenContent(state = previewTasksState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewTasksLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = TasksComponent.State(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksErrorDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = TasksComponent.State(error = "Не удалось загрузить задания"),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksErrorLight() {
    CuMobileTheme(darkTheme = false) {
        TasksScreenContent(
            state = TasksComponent.State(error = "Не удалось загрузить задания"),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTasksEmptyFiltersDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = previewTasksState.copy(
                searchQuery = "несуществующий запрос",
            ),
            onIntent = {},
        )
    }
}

private val previewTasksArchiveState = TasksComponent.State(
    segment = 1,
    allTasks = listOf(
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
    ),
)

@Preview
@Composable
private fun PreviewTasksArchiveDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(state = previewTasksArchiveState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewTasksWithFiltersDark() {
    CuMobileTheme(darkTheme = true) {
        TasksScreenContent(
            state = previewTasksState.copy(
                statusFilter = TaskState.InProgress,
                courseFilter = "1",
            ),
            onIntent = {},
        )
    }
}
