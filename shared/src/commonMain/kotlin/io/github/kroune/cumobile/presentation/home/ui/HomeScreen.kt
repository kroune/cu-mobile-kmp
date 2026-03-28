@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.formatEpochDate
import io.github.kroune.cumobile.presentation.common.isError
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CourseCard
import io.github.kroune.cumobile.presentation.common.ui.DeadlineTaskCard
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.home.HomeComponent

/**
 * Home screen composable for the "Главная" tab.
 *
 * Renders two scrollable sections:
 * 1. **Deadlines** — horizontal row of task cards.
 * 2. **Courses** — 2-column grid of course cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is HomeComponent.Effect.ShowError -> {
                    actionError = effect.message
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isContentLoading,
        onRefresh = { component.onIntent(HomeComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        when {
            state.tasks.isError && state.courses.isError -> ErrorContent(
                error = "Не удалось загрузить данные",
                onRetry = { component.onIntent(HomeComponent.Intent.Refresh) },
            )
            state.isContentLoading -> HomeScreenSkeleton()
            else -> Column(modifier = Modifier.fillMaxSize()) {
                ActionErrorBar(
                    error = actionError,
                    onDismiss = { actionError = null },
                )
                HomeContent(
                    state = state,
                    onIntent = { component.onIntent(it) },
                    onTaskClick = { task ->
                        component.onIntent(HomeComponent.Intent.OpenTask(task))
                    },
                    onCourseClick = { courseId ->
                        component.onIntent(HomeComponent.Intent.OpenCourse(courseId))
                    },
                )
            }
        }
    }
}

@Composable
internal fun HomeContent(
    state: HomeComponent.State,
    onIntent: (HomeComponent.Intent) -> Unit,
    onTaskClick: (StudentTask) -> Unit,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            DeadlinesSection(
                tasksState = state.tasks,
                deadlineTasks = state.deadlineTasks,
                onTaskClick = onTaskClick,
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ScheduleSection(
                state = state,
                onIntent = onIntent,
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CoursesSection(
                coursesState = state.courses,
                activeCourses = state.activeCourses,
                onCourseClick = onCourseClick,
            )
        }
    }
}

/**
 * Deadlines section: header + horizontally scrollable task cards.
 */
@Composable
private fun DeadlinesSection(
    tasksState: ContentState<List<StudentTask>>,
    deadlineTasks: List<StudentTask>,
    onTaskClick: (StudentTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        SectionHeader(
            title = "Дедлайны",
            count = deadlineTasks.size,
        )

        when (tasksState) {
            is ContentState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SkeletonHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(SkeletonCardSpacing),
                ) {
                    repeat(SkeletonTaskCardCount) {
                        DeadlineTaskCardSkeleton()
                    }
                }
            }
            is ContentState.Error -> EmptySection(text = "Не удалось загрузить задания")
            is ContentState.Success -> {
                if (deadlineTasks.isEmpty()) {
                    EmptySection(text = "Нет активных заданий")
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = deadlineTasks,
                            key = { it.id },
                        ) { task ->
                            DeadlineTaskCard(
                                task = task,
                                onClick = { onTaskClick(task) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Courses section: header + 2-column grid of course cards.
 *
 * The grid height is calculated based on the number of rows
 * to avoid nested scrollable containers.
 */
@Composable
private fun CoursesSection(
    coursesState: ContentState<List<Course>>,
    activeCourses: List<Course>,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Курсы",
            count = activeCourses.size,
        )

        when (coursesState) {
            is ContentState.Loading -> {
                Row(
                    modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(SkeletonCardSpacing),
                ) {
                    repeat(2) {
                        CourseCardSkeleton(
                            Modifier.weight(1f).aspectRatio(CourseCardAspectRatio),
                        )
                    }
                }
            }
            is ContentState.Error -> EmptySection(text = "Не удалось загрузить курсы")
            is ContentState.Success -> {
                if (activeCourses.isEmpty()) {
                    EmptySection(text = "Нет активных курсов")
                } else {
                    // Fixed-height grid to avoid nested scroll conflict
                    val rowCount = (activeCourses.size + 1) / 2
                    val itemHeight = 120.dp
                    val spacing = 12.dp
                    val gridHeight = (itemHeight * rowCount) + (spacing * maxOf(0, rowCount - 1))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        userScrollEnabled = false,
                        modifier = Modifier.height(gridHeight),
                    ) {
                        items(
                            items = activeCourses,
                            key = { it.id },
                        ) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onCourseClick(course.id) },
                                modifier = Modifier.aspectRatio(1.4f),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Schedule section: date navigation + list of classes.
 */
@Composable
private fun ScheduleSection(
    state: HomeComponent.State,
    onIntent: (HomeComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Расписание",
            count = 0,
        )

        DateNavigationRow(
            selectedDateMillis = state.selectedDateMillis,
            onIntent = onIntent,
        )

        when (val schedule = state.schedule) {
            is ContentState.Loading ->
                EmptySection(text = "Загрузка расписания…")
            is ContentState.Error ->
                EmptySection(text = schedule.message)
            is ContentState.Success -> {
                if (schedule.data.isEmpty()) {
                    EmptySection(text = "Нет занятий на этот день")
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        schedule.data.forEach { classData ->
                            ClassCard(classData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateNavigationRow(
    selectedDateMillis: Long,
    onIntent: (HomeComponent.Intent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onIntent(HomeComponent.Intent.PreviousDay) }) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Предыдущий день",
                tint = AppTheme.colors.accent,
            )
        }

        Text(
            text = formatEpochDate(selectedDateMillis),
            color = AppTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onIntent(HomeComponent.Intent.Today) },
        )

        IconButton(onClick = { onIntent(HomeComponent.Intent.NextDay) }) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Следующий день",
                tint = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun ClassCard(classData: ClassData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = classData.startTime,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = classData.endTime,
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(AppTheme.colors.accent),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = classData.title,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (classData.room.isNotEmpty()) {
                    Text(
                        text = "📍 ${classData.room}",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = "🏷️ ${classData.type}",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = AppTheme.colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(AppTheme.colors.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$count",
                    color = AppTheme.colors.background,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * Empty state placeholder for a section.
 */
@Composable
private fun EmptySection(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
    }
}

private const val SkeletonTaskCardCount = 3
private const val SkeletonClassCardCount = 2
private val SkeletonCardSpacing = 12.dp
private val SkeletonSectionSpacing = 16.dp
private val SkeletonHorizontalPadding = 16.dp
private val SkeletonScheduleItemSpacing = 8.dp
private const val CourseCardAspectRatio = 1.4f

/**
 * Skeleton loading state for the Home screen.
 *
 * Shows shimmer placeholders matching the real layout:
 * deadlines row, schedule section, and courses grid.
 */
@Composable
internal fun HomeScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Deadlines section
        SectionHeader(title = "Дедлайны", count = 0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SkeletonHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(SkeletonCardSpacing),
        ) {
            repeat(SkeletonTaskCardCount) {
                DeadlineTaskCardSkeleton()
            }
        }

        Spacer(Modifier.height(SkeletonSectionSpacing))

        // Schedule section
        SectionHeader(title = "Расписание", count = 0)
        Column(
            modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(SkeletonScheduleItemSpacing),
        ) {
            repeat(SkeletonClassCardCount) {
                ClassCardSkeleton()
            }
        }

        Spacer(Modifier.height(SkeletonSectionSpacing))

        // Courses section
        SectionHeader(title = "Курсы", count = 0)
        Row(
            modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(SkeletonCardSpacing),
        ) {
            repeat(2) {
                CourseCardSkeleton(Modifier.weight(1f).aspectRatio(CourseCardAspectRatio))
            }
        }
        Spacer(Modifier.height(SkeletonCardSpacing))
        Row(
            modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(SkeletonCardSpacing),
        ) {
            repeat(2) {
                CourseCardSkeleton(Modifier.weight(1f).aspectRatio(CourseCardAspectRatio))
            }
        }

        Spacer(Modifier.height(SkeletonSectionSpacing))
    }
}
