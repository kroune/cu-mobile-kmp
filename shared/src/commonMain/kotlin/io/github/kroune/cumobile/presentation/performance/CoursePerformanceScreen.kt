@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.SegmentedControl
import io.github.kroune.cumobile.presentation.common.TopBar
import io.github.kroune.cumobile.presentation.common.gradeColor
import io.github.kroune.cumobile.presentation.common.gradeDescription

/**
 * Course performance screen with two tabs:
 * "Набранные баллы" (Scores) and "Успеваемость" (Performance).
 */
@Composable
fun CoursePerformanceScreen(
    component: CoursePerformanceComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        TopBar(
            title = "Успеваемость",
            profileInitials = "",
            lateDaysBalance = null,
            onNotificationsClick = {},
            onProfileClick = {},
        )
        TextButton(onClick = onBack) {
            Text("← Назад", color = AppColors.Accent)
        }

        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = {
                    component.onIntent(CoursePerformanceComponent.Intent.Refresh)
                },
            )
            else -> PerformanceContent(state = state, component = component)
        }
    }
}

@Composable
private fun PerformanceContent(
    state: CoursePerformanceComponent.State,
    component: CoursePerformanceComponent,
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
                component.onIntent(CoursePerformanceComponent.Intent.SelectTab(it))
            },
        )
        when (state.selectedTab) {
            0 -> ScoresTab(
                exercises = state.filteredExercises,
                activityNames = state.activityNames,
                activeFilter = state.activityFilter,
                onFilterActivity = {
                    component.onIntent(
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
            .background(AppColors.Surface)
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
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
            )
            Text(
                text = gradeDescription(grade),
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (courseName.isNotEmpty()) {
                Text(
                    text = courseName,
                    color = AppColors.TextSecondary,
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
