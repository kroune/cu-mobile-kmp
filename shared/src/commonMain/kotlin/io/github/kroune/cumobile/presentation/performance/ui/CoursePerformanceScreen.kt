package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.SegmentedControl
import io.github.kroune.cumobile.presentation.common.ui.gradeColor
import io.github.kroune.cumobile.presentation.common.ui.gradeDescription
import io.github.kroune.cumobile.presentation.performance.CoursePerformanceComponent
import kotlinx.collections.immutable.persistentListOf

/**
 * Course performance screen with two tabs:
 * "Набранные баллы" (Scores) and "Успеваемость" (Performance).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePerformanceScreen(
    component: CoursePerformanceComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    CoursePerformanceScreenContent(
        state = state,
        onIntent = component::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoursePerformanceScreenContent(
    state: CoursePerformanceComponent.State,
    onIntent: (CoursePerformanceComponent.Intent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isContentLoading,
        onRefresh = { onIntent(CoursePerformanceComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            DetailTopBar(
                title = "Успеваемость",
                onBack = onBack,
            )

            when (val content = state.content) {
                is ContentState.Loading -> CoursePerformanceScreenSkeleton()
                is ContentState.Error -> ErrorContent(
                    error = content.message,
                    onRetry = {
                        onIntent(CoursePerformanceComponent.Intent.Refresh)
                    },
                )
                is ContentState.Success -> PerformanceContent(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun PerformanceContent(
    state: CoursePerformanceComponent.State,
    onIntent: (CoursePerformanceComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabLabels = persistentListOf("Набранные баллы", "Успеваемость")
    Column(modifier = modifier.fillMaxSize()) {
        TotalGradeCard(
            grade = state.totalGrade,
            courseName = state.courseName,
        )
        SegmentedControl(
            labels = tabLabels,
            selectedIndex = state.selectedTab,
            onSelect = {
                onIntent(CoursePerformanceComponent.Intent.SelectTab(it))
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        when (state.selectedTab) {
            0 -> ScoresTab(
                exercises = state.filteredExercises,
                activityNames = state.activityNames,
                activeFilter = state.activityFilter,
                onFilterActivity = {
                    onIntent(
                        CoursePerformanceComponent.Intent.FilterByActivity(it),
                    )
                },
            )
            1 -> PerformanceTab(
                summaries = state.activitySummaries,
                totalContribution = state.totalContribution,
            )
        }
    }
}

// region Total Grade Card

@Composable
private fun TotalGradeCard(
    grade: Int,
    courseName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradeColor(grade)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = grade.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "Итоговая оценка",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
            Text(
                text = gradeDescription(grade),
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (courseName.isNotEmpty()) {
                Text(
                    text = courseName,
                    color = AppTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// endregion

// region Helpers

/** Format a score, showing one decimal place only if needed. */
@Suppress("MagicNumber")
internal fun formatScore(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/** Color based on score ratio (0.0 to 1.0). */
@Suppress("MagicNumber")
internal fun scoreRatioColor(ratio: Double): Color =
    when {
        ratio >= 0.8 -> Color(0xFF66BB6A)
        ratio >= 0.6 -> Color(0xFFFFEB3B)
        ratio >= 0.4 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }

// endregion

// region Skeleton

private const val SkeletonExerciseCount = 4
private val SkeletonExerciseSpacing = 8.dp
private val SkeletonHorizontalPadding = 16.dp

/**
 * Skeleton loading state for the Course Performance screen.
 *
 * Shows shimmer placeholders for the total grade card,
 * segmented control, and exercise tiles.
 * The [DetailTopBar] is already rendered above the when-block.
 */
@Composable
private fun CoursePerformanceScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        TotalGradeCardSkeleton()
        SegmentedControl(
            labels = persistentListOf("Набранные баллы", "Успеваемость"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SkeletonHorizontalPadding),
        ) {
            repeat(SkeletonExerciseCount) {
                ExerciseTileSkeleton()
                Spacer(Modifier.height(SkeletonExerciseSpacing))
            }
        }
    }
}

// endregion
