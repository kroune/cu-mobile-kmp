package io.github.kroune.cumobile.presentation.courses.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.GradebookGrade
import io.github.kroune.cumobile.data.model.GradebookSemester
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.EmptyContent
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.courses.CoursesComponent

private const val SkeletonTileCount = 3

/**
 * Segment 2: Record Book ("Зачетка").
 *
 * Shows semester cards with grades.
 */
@Composable
internal fun GradebookContent(
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (val gbState = state.gradebook) {
        is ContentState.Loading -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(SkeletonTileCount) {
                    CourseListTileSkeleton()
                }
            }
        }
        is ContentState.Error -> ErrorContent(
            error = gbState.message,
            onRetry = { onIntent(CoursesComponent.Intent.Refresh) },
        )
        is ContentState.Success -> {
            val gradebook = gbState.data
            if (gradebook == null || gradebook.semesters.isEmpty()) {
                EmptyContent(text = "Нет данных по зачетке")
                return
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = gradebook.semesters,
                    key = { "${it.year}_${it.semesterNumber}" },
                ) { semester ->
                    SemesterCard(semester = semester)
                }
            }
        }
    }
}

@Composable
private fun SemesterCard(
    semester: GradebookSemester,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
    ) {
        Text(
            text = "${semester.year}/${semester.year + 1}, семестр ${semester.semesterNumber}",
            color = AppTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        semester.regularGrades.forEach { grade ->
            GradeRow(grade = grade)
        }

        if (semester.electiveGrades.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = AppTheme.colors.textSecondary.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Элективы",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            semester.electiveGrades.forEach { grade ->
                GradeRow(grade = grade)
            }
        }
    }
}

@Composable
private fun GradeRow(
    grade: GradebookGrade,
    modifier: Modifier = Modifier,
) {
    val color = normalizedGradeColor(grade.normalizedGrade)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = grade.subject,
                color = AppTheme.colors.textPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = assessmentTypeLabel(grade.assessmentType),
                color = AppTheme.colors.textSecondary,
                fontSize = 11.sp,
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = normalizedGradeLabel(grade.normalizedGrade),
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val gradeLabels = mapOf(
    "passed" to "Зачтено",
    "excellent" to "Отлично",
    "good" to "Хорошо",
    "satisfactory" to "Удовл.",
    "failed" to "Не сдано",
    "notPassed" to "Не сдано",
    "notCredited" to "Не сдано",
)

private val gradeColors = mapOf(
    "passed" to Color(0xFF66BB6A),
    "excellent" to Color(0xFF66BB6A),
    "good" to Color(0xFF42A5F5),
    "satisfactory" to Color(0xFFFFA726),
    "failed" to Color(0xFFEF5350),
    "notPassed" to Color(0xFFEF5350),
    "notCredited" to Color(0xFFEF5350),
)

private val DefaultGradeColor = Color(0xFF9E9E9E)

private val assessmentTypeLabels = mapOf(
    "exam" to "Экзамен",
    "credit" to "Зачет",
    "difCredit" to "Дифф. зачет",
)

private fun normalizedGradeLabel(grade: String): String =
    gradeLabels.getOrElse(grade) { "—" }

private fun normalizedGradeColor(grade: String): Color =
    gradeColors.getOrElse(grade) { DefaultGradeColor }

private fun assessmentTypeLabel(type: String): String =
    assessmentTypeLabels.getOrElse(type) { type }
