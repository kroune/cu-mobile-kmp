@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.CourseCard
import io.github.kroune.cumobile.presentation.common.DeadlineTaskCard
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.formatEpochDate

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

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { component.onIntent(HomeComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        when {
            state.isLoading && state.tasks.isEmpty() && state.courses.isEmpty() -> LoadingContent()
            state.error != null && state.tasks.isEmpty() && state.courses.isEmpty() -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { component.onIntent(HomeComponent.Intent.Refresh) },
            )
            else -> HomeContent(
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

@Composable
private fun HomeContent(
    state: HomeComponent.State,
    onIntent: (HomeComponent.Intent) -> Unit,
    onTaskClick: (StudentTask) -> Unit,
    onCourseClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState()),
    ) {
        DeadlinesSection(
            tasks = state.deadlineTasks,
            onTaskClick = onTaskClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScheduleSection(
            state = state,
            onIntent = onIntent
        )

        Spacer(modifier = Modifier.height(16.dp))

        CoursesSection(
            courses = state.activeCourses,
            onCourseClick = onCourseClick,
        )

        // Bottom padding for content above nav bar
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Deadlines section: header + horizontally scrollable task cards.
 */
@Composable
private fun DeadlinesSection(
    tasks: List<StudentTask>,
    onTaskClick: (StudentTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        SectionHeader(
            title = "Дедлайны",
            count = tasks.size,
        )

        if (tasks.isEmpty()) {
            EmptySection(text = "Нет активных заданий")
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = tasks,
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

/**
 * Courses section: header + 2-column grid of course cards.
 *
 * The grid height is calculated based on the number of rows
 * to avoid nested scrollable containers.
 */
@Composable
private fun CoursesSection(
    courses: List<Course>,
    onCourseClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Курсы",
            count = courses.size,
        )

        if (courses.isEmpty()) {
            EmptySection(text = "Нет активных курсов")
        } else {
            // Fixed-height grid to avoid nested scroll conflict
            val rowCount = (courses.size + 1) / 2
            val itemHeight = 120.dp
            val spacing = 12.dp
            val gridHeight = (itemHeight * rowCount) + (spacing * (rowCount - 1))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                userScrollEnabled = false,
                modifier = Modifier.height(gridHeight),
            ) {
                items(
                    items = courses,
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

        if (!state.isCalendarConnected) {
            ConnectCalendarSection(onIntent)
        } else if (state.classes.isEmpty()) {
            EmptySection(text = "Нет занятий на этот день")
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.classes.forEach { classData ->
                    ClassCard(classData)
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
        TextButton(onClick = { onIntent(HomeComponent.Intent.PreviousDay) }) {
            Text("<", color = AppColors.Accent, fontWeight = FontWeight.Bold)
        }

        Text(
            text = formatEpochDate(selectedDateMillis),
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onIntent(HomeComponent.Intent.Today) }
        )

        TextButton(onClick = { onIntent(HomeComponent.Intent.NextDay) }) {
            Text(">", color = AppColors.Accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ClassCard(classData: ClassData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = classData.startTime,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = classData.endTime,
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(AppColors.Accent)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = classData.title,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (classData.room.isNotEmpty()) {
                    Text(
                        text = "📍 ${classData.room}",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = "🏷️ ${classData.type}",
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun ConnectCalendarSection(
    onIntent: (HomeComponent.Intent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Календарь не подключен",
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
        )
        TextButton(
            onClick = { onIntent(HomeComponent.Intent.OpenProfile) },
        ) {
            Text(
                text = "Подключить в настройках профиля",
                color = AppColors.Accent,
                fontSize = 13.sp,
            )
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
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .background(
                        AppColors.Accent,
                        RoundedCornerShape(12.dp),
                    ).padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$count",
                    color = AppColors.Background,
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
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
        )
    }
}
