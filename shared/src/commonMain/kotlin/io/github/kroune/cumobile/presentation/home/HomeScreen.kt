@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.CourseCard
import io.github.kroune.cumobile.presentation.common.DeadlineTaskCard
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent

/**
 * Home screen composable for the "Главная" tab.
 *
 * Renders two scrollable sections:
 * 1. **Deadlines** — horizontal row of task cards.
 * 2. **Courses** — 2-column grid of course cards.
 */
@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    when {
        state.isLoading -> LoadingContent(
            modifier = modifier.background(AppColors.Background),
        )
        state.error != null -> ErrorContent(
            error = state.error.orEmpty(),
            onRetry = null,
            modifier = modifier.background(AppColors.Background),
        )
        else -> HomeContent(
            state = state,
            onTaskClick = { task ->
                component.onIntent(HomeComponent.Intent.OpenTask(task))
            },
            onCourseClick = { courseId ->
                component.onIntent(HomeComponent.Intent.OpenCourse(courseId))
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeComponent.State,
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
 * Section header with title and count badge.
 */
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
