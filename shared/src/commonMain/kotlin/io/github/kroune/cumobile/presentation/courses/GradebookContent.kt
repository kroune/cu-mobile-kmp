@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.courses

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
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.EmptyContent

/**
 * Segment 2: Record Book ("Зачетка").
 *
 * Shows semester cards with grades.
 */
@Composable
internal fun GradebookContent(
    state: CoursesComponent.State,
    modifier: Modifier = Modifier,
) {
    val gradebook = state.gradebook
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Элективы",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
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

/** Normalized grade label. */
private fun normalizedGradeLabel(grade: String): String =
    when (grade) {
        "passed" -> "Зачтено"
        "excellent" -> "Отлично"
        "good" -> "Хорошо"
        "satisfactory" -> "Удовл."
        "failed" -> "Не сдано"
        else -> grade
    }

/** Normalized grade color. */
private fun normalizedGradeColor(grade: String): Color =
    when (grade) {
        "passed", "excellent" -> Color(0xFF66BB6A)
        "good" -> Color(0xFF42A5F5)
        "satisfactory" -> Color(0xFFFFA726)
        "failed" -> Color(0xFFEF5350)
        else -> Color(0xFF9E9E9E)
    }

/** Assessment type label. */
private fun assessmentTypeLabel(type: String): String =
    when (type) {
        "exam" -> "Экзамен"
        "credit" -> "Зачет"
        "difCredit" -> "Дифф. зачет"
        else -> type
    }
