@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.courses

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.SegmentedControl
import io.github.kroune.cumobile.presentation.common.courseCategoryColor
import io.github.kroune.cumobile.presentation.common.courseCategoryLabel
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix

/**
 * Courses tab screen with 3 segments: Courses, Grade Sheet, Record Book.
 *
 * Matches the Flutter reference CoursesTab layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    component: CoursesComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    CoursesScreenContent(
        state = state,
        onIntent = component::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoursesScreenContent(
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onIntent(CoursesComponent.Intent.Refresh) },
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
                labels = listOf("Курсы", "Ведомость", "Зачетка"),
                selectedIndex = state.segment,
                onSelect = { onIntent(CoursesComponent.Intent.SelectSegment(it)) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading && state.courses.isEmpty() -> LoadingContent()
                state.error != null && state.courses.isEmpty() -> ErrorContent(
                    error = state.error.orEmpty(),
                    onRetry = { onIntent(CoursesComponent.Intent.Refresh) },
                )
                else -> when (state.segment) {
                    0 -> CoursesListContent(state = state, onIntent = onIntent)
                    1 -> GradeSheetContent(state = state, onIntent = onIntent)
                    2 -> GradebookContent(state = state)
                }
            }
        }
    }
}

// region Segment 0: Courses list

@Composable
private fun CoursesListContent(
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = activeCourses(state.courses, state.courseOrder)
    val archived = archivedCourses(state.courses, state.courseOrder)

    if (active.isEmpty() && archived.isEmpty()) {
        EmptyContent(text = "Нет курсов")
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (active.isNotEmpty()) {
            item {
                EditModeToggle(
                    isEditMode = state.isEditMode,
                    onClick = { onIntent(CoursesComponent.Intent.ToggleEditMode) },
                )
            }
        }

        items(items = active, key = { it.id }) { course ->
            ActiveCourseItem(
                course = course,
                active = active,
                state = state,
                onIntent = onIntent,
            )
        }

        if (archived.isNotEmpty()) {
            item(key = "archived_header") {
                ArchivedHeader(
                    count = archived.size,
                    expanded = state.showArchived,
                    onClick = { onIntent(CoursesComponent.Intent.ToggleArchived) },
                )
            }

            if (state.showArchived) {
                items(items = archived, key = { it.id }) { course ->
                    CourseListTile(
                        course = course,
                        onClick = {
                            onIntent(CoursesComponent.Intent.OpenCourse(course.id))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditModeToggle(
    isEditMode: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = if (isEditMode) "Готово" else "Изменить",
                color = AppTheme.colors.accent,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ActiveCourseItem(
    course: Course,
    active: List<Course>,
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
) {
    CourseListTile(
        course = course,
        isEditMode = state.isEditMode,
        onMoveUp = {
            val index = active.indexOf(course)
            if (index > 0) {
                val newOrder = swapIds(active, index, index - 1)
                onIntent(CoursesComponent.Intent.ReorderCourses(newOrder))
            }
        },
        onMoveDown = {
            val index = active.indexOf(course)
            if (index < active.size - 1) {
                val newOrder = swapIds(active, index, index + 1)
                onIntent(CoursesComponent.Intent.ReorderCourses(newOrder))
            }
        },
        onClick = {
            onIntent(CoursesComponent.Intent.OpenCourse(course.id))
        },
    )
}

private fun swapIds(courses: List<Course>, from: Int, to: Int): List<Int> {
    val ids = courses.map { it.id }.toMutableList()
    val temp = ids[from]
    ids[from] = ids[to]
    ids[to] = temp
    return ids
}

/**
 * A single course row: category icon + name + category label + chevron.
 */
@Composable
private fun CourseListTile(
    course: Course,
    onClick: () -> Unit,
    isEditMode: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val catColor = courseCategoryColor(course.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isEditMode) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\u25B2", // Up arrow
                    modifier = Modifier.clickable { onMoveUp() }.padding(4.dp),
                    color = AppTheme.colors.accent,
                )
                Text(
                    text = "\u25BC", // Down arrow
                    modifier = Modifier.clickable { onMoveDown() }.padding(4.dp),
                    color = AppTheme.colors.accent,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(catColor),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stripEmojiPrefix(course.name),
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = courseCategoryLabel(course.category),
                color = catColor,
                fontSize = 12.sp,
            )
        }

        Text(
            text = "\u203A",
            color = AppTheme.colors.textSecondary,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun ArchivedHeader(
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Архив",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppTheme.colors.textSecondary.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (expanded) "\u25B2" else "\u25BC",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
    }
}

// endregion

private val previewCoursesState = CoursesComponent.State(
    courses = listOf(
        Course(id = 1, name = "Алгоритмы и структуры данных", category = "development"),
        Course(id = 2, name = "Линейная алгебра", category = "mathematics"),
        Course(id = 3, name = "Управление проектами", category = "business"),
        Course(id = 4, name = "Физика", category = "stem"),
    ),
)

@Preview
@Composable
private fun PreviewCoursesScreenDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(state = previewCoursesState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewCoursesScreenLight() {
    CuMobileTheme(darkTheme = false) {
        CoursesScreenContent(state = previewCoursesState, onIntent = {})
    }
}
