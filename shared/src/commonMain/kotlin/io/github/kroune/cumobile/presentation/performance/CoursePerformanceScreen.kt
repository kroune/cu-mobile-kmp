@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.performance

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.CourseExerciseActivity
import io.github.kroune.cumobile.data.model.CourseExerciseTheme
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.data.model.TaskScoreActivity
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.ExerciseTileSkeleton
import io.github.kroune.cumobile.presentation.common.SegmentedControl
import io.github.kroune.cumobile.presentation.common.TotalGradeCardSkeleton
import io.github.kroune.cumobile.presentation.common.gradeColor
import io.github.kroune.cumobile.presentation.common.gradeDescription

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
        isRefreshing = state.isLoading,
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

            when {
                state.isLoading && state.exercises.isEmpty() ->
                    CoursePerformanceScreenSkeleton()
                state.error != null && state.exercises.isEmpty() -> ErrorContent(
                    error = state.error,
                    onRetry = {
                        onIntent(CoursePerformanceComponent.Intent.Refresh)
                    },
                )
                else -> PerformanceContent(state = state, onIntent = onIntent)
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
    val tabLabels = listOf("Набранные баллы", "Успеваемость")
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
internal fun formatScore(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/** Color based on score ratio (0.0 to 1.0). */
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
            labels = listOf("Набранные баллы", "Успеваемость"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.padding(horizontal = SkeletonHorizontalPadding),
        )
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

@Preview
@Composable
private fun PreviewPerformanceScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1", isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1", isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

// endregion

// region Previews

private val previewPerformanceExercises =
    listOf(
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "1",
                name = "ДЗ: Быстрая сортировка",
                activity = CourseExerciseActivity(id = "1", name = "Домашнее задание"),
                theme = CourseExerciseTheme(id = "1", name = "Сортировки"),
            ),
            score = TaskScore(
                id = "1",
                score = 8.0,
                maxScore = 10,
                exerciseId = "1",
                state = "evaluated",
                activity = TaskScoreActivity(
                    id = "1",
                    name = "Домашнее задание",
                    weight = 0.4,
                ),
            ),
        ),
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "2",
                name = "Лабораторная: Хеш-таблицы",
                activity = CourseExerciseActivity(id = "2", name = "Лабораторная"),
                theme = CourseExerciseTheme(id = "2", name = "Хеширование"),
            ),
            score = TaskScore(
                id = "2",
                score = 5.0,
                maxScore = 10,
                exerciseId = "2",
                state = "evaluated",
                activity = TaskScoreActivity(
                    id = "2",
                    name = "Лабораторная",
                    weight = 0.3,
                ),
            ),
        ),
        ExerciseWithScore(
            exercise = CourseExercise(
                id = "3",
                name = "Контрольная: Графы",
                activity = CourseExerciseActivity(id = "1", name = "Домашнее задание"),
                theme = CourseExerciseTheme(id = "3", name = "Графы"),
            ),
            score = null,
        ),
    )

private val previewPerformanceState =
    CoursePerformanceComponent.State(
        courseId = "1",
        courseName = "Алгоритмы и структуры данных",
        totalGrade = 7,
        exercises = previewPerformanceExercises,
        activitySummaries = listOf(
            ActivitySummary(
                activityId = "1",
                activityName = "Домашнее задание",
                count = 5,
                averageScore = 8.0,
                weight = 0.4,
            ),
            ActivitySummary(
                activityId = "2",
                activityName = "Лабораторная",
                count = 3,
                averageScore = 5.0,
                weight = 0.3,
            ),
        ),
    )

@Preview
@Composable
private fun PreviewPerformanceScoresDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceScoresLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceTabDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = previewPerformanceState.copy(selectedTab = 1),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(courseId = "1", isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceErrorDark() {
    CuMobileTheme(darkTheme = true) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(
                courseId = "1",
                error = "Не удалось загрузить успеваемость",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewPerformanceErrorLight() {
    CuMobileTheme(darkTheme = false) {
        CoursePerformanceScreenContent(
            state = CoursePerformanceComponent.State(
                courseId = "1",
                error = "Не удалось загрузить успеваемость",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

// endregion
