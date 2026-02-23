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
import androidx.compose.material3.Text
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
import io.github.kroune.cumobile.presentation.common.AppColors
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
@Composable
fun CoursesScreen(
    component: CoursesComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SegmentedControl(
            labels = listOf("Курсы", "Ведомость", "Зачетка"),
            selectedIndex = state.segment,
            onSelect = {
                component.onIntent(CoursesComponent.Intent.SelectSegment(it))
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { component.onIntent(CoursesComponent.Intent.Refresh) },
            )
            else -> when (state.segment) {
                0 -> CoursesListContent(state = state, component = component)
                1 -> GradeSheetContent(state = state, component = component)
                2 -> GradebookContent(state = state)
            }
        }
    }
}

// region Segment 0: Courses list

@Composable
private fun CoursesListContent(
    state: CoursesComponent.State,
    component: CoursesComponent,
    modifier: Modifier = Modifier,
) {
    val active = activeCourses(state.courses)
    val archived = archivedCourses(state.courses)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items = active, key = { it.id }) { course ->
            CourseListTile(
                course = course,
                onClick = {
                    component.onIntent(CoursesComponent.Intent.OpenCourse(course.id))
                },
            )
        }

        if (archived.isNotEmpty()) {
            item(key = "archived_header") {
                ArchivedHeader(
                    count = archived.size,
                    expanded = state.showArchived,
                    onClick = {
                        component.onIntent(CoursesComponent.Intent.ToggleArchived)
                    },
                )
            }

            if (state.showArchived) {
                items(items = archived, key = { it.id }) { course ->
                    CourseListTile(
                        course = course,
                        onClick = {
                            component.onIntent(
                                CoursesComponent.Intent.OpenCourse(course.id),
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * A single course row: category icon + name + category label + chevron.
 */
@Composable
private fun CourseListTile(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val catColor = courseCategoryColor(course.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                color = AppColors.TextPrimary,
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
            color = AppColors.TextSecondary,
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
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.TextSecondary.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (expanded) "\u25B2" else "\u25BC",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

// endregion
