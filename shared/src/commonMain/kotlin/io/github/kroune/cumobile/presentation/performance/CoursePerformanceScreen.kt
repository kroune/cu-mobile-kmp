package io.github.kroune.cumobile.presentation.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
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
import io.github.kroune.cumobile.presentation.common.TopBar

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
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                error = state.error!!,
                onRetry = {
                    component.onIntent(CoursePerformanceComponent.Intent.Refresh)
                },
            )
            else -> PerformanceContent(state = state, component = component)
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppColors.Accent)
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(error, color = AppColors.Error, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("Повторить", color = AppColors.Accent)
            }
        }
    }
}

@Composable
private fun PerformanceContent(
    state: CoursePerformanceComponent.State,
    component: CoursePerformanceComponent,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TotalGradeCard(
            grade = state.totalGrade,
            courseName = state.courseName,
        )
        TabSelector(
            selectedTab = state.selectedTab,
            onTabSelected = {
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

// region Tab Selector

@Composable
private fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf("Набранные баллы", "Успеваемость")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val bgColor = if (isSelected) {
                AppColors.Accent.copy(alpha = 0.2f)
            } else {
                Color.Transparent
            }
            val textColor = if (isSelected) {
                AppColors.Accent
            } else {
                AppColors.TextSecondary
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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

/** Color-code a grade value (0-10 scale). */
internal fun gradeColor(grade: Int): Color =
    when {
        grade >= 8 -> Color(0xFF66BB6A) // green
        grade >= 6 -> Color(0xFFFFEB3B) // yellow
        grade >= 4 -> Color(0xFFFFA726) // orange
        else -> Color(0xFFEF5350) // red
    }

/** Human-readable grade description. */
internal fun gradeDescription(grade: Int): String =
    when {
        grade >= 8 -> "Отлично"
        grade >= 6 -> "Хорошо"
        grade >= 4 -> "Удовлетворительно"
        else -> "Неудовлетворительно"
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
