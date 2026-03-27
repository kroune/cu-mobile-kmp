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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.StudentPerformanceCourse
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.gradeColor
import io.github.kroune.cumobile.presentation.common.gradeDescription
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix

private const val SkeletonTileCount = 4

/**
 * Segment 1: Grade Sheet ("Ведомость").
 *
 * Shows performance grades for active (non-archived) courses.
 */
@Composable
internal fun GradeSheetContent(
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val perfState = state.performanceCourses) {
        is ContentState.Loading -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(SkeletonTileCount) {
                    CourseListTileSkeleton()
                }
            }
        }
        is ContentState.Error -> ErrorContent(
            error = perfState.message,
            onRetry = { onIntent(CoursesComponent.Intent.Refresh) },
        )
        is ContentState.Success -> {
            val items = perfState.data.filter { perf ->
                state.courseList.none { it.id == perf.id && it.isArchived }
            }

            if (items.isEmpty()) {
                EmptyContent(text = "Нет данных по ведомости")
                return
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items = items, key = { it.id }) { perf ->
                    GradeSheetTile(
                        performance = perf,
                        onClick = {
                            onIntent(
                                CoursesComponent.Intent.OpenCoursePerformance(
                                    courseId = perf.id,
                                    courseName = perf.name,
                                    totalGrade = perf.total,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeSheetTile(
    performance: StudentPerformanceCourse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = gradeColor(performance.total)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = performance.total.toString(),
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stripEmojiPrefix(performance.name),
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = gradeDescription(performance.total),
                color = color,
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
