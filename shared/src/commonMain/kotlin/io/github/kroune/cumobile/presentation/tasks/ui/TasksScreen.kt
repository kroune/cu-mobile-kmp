package io.github.kroune.cumobile.presentation.tasks.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.errorOrNull
import io.github.kroune.cumobile.presentation.common.isLoading
import io.github.kroune.cumobile.presentation.common.ui.AppTabRow
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.EmptyContent
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.LoadingContent
import io.github.kroune.cumobile.presentation.common.ui.stripEmojiPrefix
import io.github.kroune.cumobile.presentation.common.ui.taskStateLabel
import io.github.kroune.cumobile.presentation.tasks.TasksComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    val content = state.content.dataOrNull
    PullToRefreshBox(
        isRefreshing = state.content.isLoading,
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
            val pagerState = rememberPagerState(initialPage = state.segment) { 2 }

            LaunchedEffect(state.segment) {
                if (pagerState.currentPage != state.segment) {
                    pagerState.animateScrollToPage(state.segment)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppTabRow(
                currentPage = pagerState.currentPage,
                labels = listOf(
                    "активные" + (content?.let { " (${it.activeCount})" }.orEmpty()),
                    "архив" + (content?.let { " (${it.archiveCount})" }.orEmpty()),
                ),
                onPageSelected = { page ->
                    onIntent(TasksComponent.Intent.SelectSegment(page))
                },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false,
            ) { page ->
                val pageTasks = when (page) {
                    0 -> content?.activeFilteredTasks ?: persistentListOf()
                    else -> content?.archiveFilteredTasks ?: persistentListOf()
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    FiltersRow(
                        state = state,
                        content = content,
                        onIntent = onIntent,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TasksContentArea(
                        contentState = state.content,
                        tasks = pageTasks,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Content area showing loading, error, empty, or task list states.
 */
@Composable
private fun TasksContentArea(
    contentState: ContentState<TasksComponent.Content>,
    tasks: ImmutableList<StudentTask>,
    onIntent: (TasksComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error = contentState.errorOrNull
    when {
        contentState.isLoading && tasks.isEmpty() -> LoadingContent(modifier)
        error != null && tasks.isEmpty() -> ErrorContent(
            error = error,
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
    content: TasksComponent.Content?,
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
                .background(AppTheme.colors.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
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
            availableStatuses = content?.availableStatuses ?: persistentListOf(),
            statusFilter = state.statusFilter,
            onIntent = onIntent,
        )

        CourseFilterChips(
            availableCourses = content?.availableCourses ?: persistentListOf(),
            courseFilter = state.courseFilter,
            onIntent = onIntent,
        )
    }
}

/** Status filter chips row — selected chip moves to front. */
@Composable
private fun StatusFilterChips(
    availableStatuses: ImmutableList<String>,
    statusFilter: String?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    val statuses = availableStatuses
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
    availableCourses: ImmutableList<Pair<String, String>>,
    courseFilter: String?,
    onIntent: (TasksComponent.Intent) -> Unit,
) {
    if (availableCourses.isEmpty()) return
    val sorted = selectedFirst(availableCourses, courseFilter) { it.first }
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

/** Moves the selected string to the front, keeping relative order of the rest. */
private fun selectedFirst(
    items: List<String>,
    selectedKey: String?,
): List<String> =
    selectedFirst(items, selectedKey) { it }

/** Moves the selected item to the front, keeping relative order of the rest. */
private fun <T> selectedFirst(
    items: List<T>,
    selectedKey: String?,
    keySelector: (T) -> String,
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
            .padding(horizontal = 10.dp, vertical = 1.dp),
    ) {
        Text(
            text = label,
            color = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}
