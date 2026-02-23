package io.github.kroune.cumobile.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.AppColors
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
@Composable
fun TasksScreen(
    component: TasksComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val tasks = filteredTasks(state)
    val allTasks = state.allTasks

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Segment control
        SegmentControl(
            selectedSegment = state.segment,
            activeCount = countBySegment(allTasks, 0),
            archiveCount = countBySegment(allTasks, 1),
            onSelect = {
                component.onIntent(TasksComponent.Intent.SelectSegment(it))
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filters row
        FiltersRow(
            state = state,
            allTasks = allTasks,
            onIntent = { component.onIntent(it) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        TasksContentArea(
            state = state,
            tasks = tasks,
            onIntent = { component.onIntent(it) },
        )
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
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AppColors.Accent)
            }
        }
        state.error != null -> {
            ErrorContent(
                error = state.error,
                onRetry = { onIntent(TasksComponent.Intent.Refresh) },
                modifier = modifier,
            )
        }
        tasks.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Нет заданий по выбранным фильтрам",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                )
            }
        }
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
 * Segment control with Active/Archive tabs and counts.
 */
@Composable
private fun SegmentControl(
    selectedSegment: Int,
    activeCount: Int,
    archiveCount: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val segments = listOf(
        "Активные ($activeCount)",
        "Архив ($archiveCount)",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Surface)
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        segments.forEachIndexed { index, label ->
            val selected = selectedSegment == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (selected) {
                            AppColors.Accent.copy(alpha = 0.2f)
                        } else {
                            AppColors.Surface
                        },
                    ).clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) AppColors.Accent else AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                )
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
        // Search field
        TextField(
            value = state.searchQuery,
            onValueChange = { onIntent(TasksComponent.Intent.Search(it)) },
            placeholder = {
                Text(
                    text = "Поиск по названию...",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.Accent,
                focusedIndicatorColor = AppColors.Accent,
                unfocusedIndicatorColor = AppColors.TextSecondary.copy(
                    alpha = 0.3f,
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
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

        ResetFiltersButton(state = state, onIntent = onIntent)
    }
}

/** Status filter chips row. */
@Composable
private fun StatusFilterChips(
    allTasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    segment: Int,
    statusFilter: String?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val statuses = availableStatuses(allTasks, segment)
    if (statuses.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = statuses,
            key = { it },
        ) { status ->
            FilterChip(
                label = taskStateLabel(status),
                selected = statusFilter == status,
                onClick = {
                    val newFilter = if (statusFilter == status) null else status
                    onIntent(TasksComponent.Intent.FilterByStatus(newFilter))
                },
            )
        }
    }
}

/** Course filter chips row. */
@Composable
private fun CourseFilterChips(
    allTasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    courseFilter: Int?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val courses = availableCourses(allTasks)
    if (courses.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = courses,
            key = { it.first },
        ) { (courseId, courseName) ->
            FilterChip(
                label = stripEmojiPrefix(courseName),
                selected = courseFilter == courseId,
                onClick = {
                    val newFilter = if (courseFilter == courseId) null else courseId
                    onIntent(TasksComponent.Intent.FilterByCourse(newFilter))
                },
            )
        }
    }
}

/** Reset button shown only when filters are active. */
@Composable
private fun ResetFiltersButton(
    state: TasksComponent.State,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val hasFilters = state.statusFilter != null ||
        state.courseFilter != null ||
        state.searchQuery.isNotEmpty()
    if (!hasFilters) return
    TextButton(
        onClick = {
            onIntent(TasksComponent.Intent.FilterByStatus(null))
            onIntent(TasksComponent.Intent.FilterByCourse(null))
            onIntent(TasksComponent.Intent.Search(""))
        },
    ) {
        Text(
            text = "Сбросить фильтры",
            color = AppColors.Accent,
            fontSize = 13.sp,
        )
    }
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
                color = if (selected) AppColors.Accent else AppColors.TextSecondary,
                shape = shape,
            ).background(
                if (selected) {
                    AppColors.Accent.copy(alpha = 0.15f)
                } else {
                    AppColors.Surface
                },
            ).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.Accent else AppColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

/**
 * Error state with retry button.
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\u26A0\uFE0F", // ⚠️
                fontSize = 40.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = AppColors.Error,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text(
                    text = "Повторить",
                    color = AppColors.Accent,
                )
            }
        }
    }
}

/**
 * Counts tasks belonging to the given segment.
 */
private fun countBySegment(
    tasks: List<io.github.kroune.cumobile.data.model.StudentTask>,
    segment: Int,
): Int {
    val segmentStates = if (segment == 0) ACTIVE_STATES else ARCHIVE_STATES
    return tasks.count {
        normalizeTaskState(effectiveTaskState(it)) in segmentStates
    }
}
