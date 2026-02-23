@file:Suppress("TooManyFunctions", "MagicNumber")

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
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.GradebookGrade
import io.github.kroune.cumobile.data.model.GradebookSemester
import io.github.kroune.cumobile.data.model.StudentPerformanceCourse
import io.github.kroune.cumobile.presentation.common.AppColors
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

        CoursesSegmentControl(
            selectedSegment = state.segment,
            onSelect = {
                component.onIntent(CoursesComponent.Intent.SelectSegment(it))
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                error = state.error!!,
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

@Composable
private fun CoursesSegmentControl(
    selectedSegment: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf("Курсы", "Ведомость", "Зачетка")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Surface)
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        labels.forEachIndexed { index, label ->
            val selected = selectedSegment == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (selected) {
                            AppColors.Accent.copy(alpha = 0.2f)
                        } else {
                            AppColors.Surface
                        },
                    ).clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) AppColors.Accent else AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
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
        // Category color dot
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
            text = "\u203A", // ›
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

        // Count badge
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
            text = if (expanded) "\u25B2" else "\u25BC", // ▲ / ▼
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

// endregion

// region Segment 1: Grade Sheet (Ведомость)

@Composable
private fun GradeSheetContent(
    state: CoursesComponent.State,
    component: CoursesComponent,
    modifier: Modifier = Modifier,
) {
    val items = state.performanceCourses.filter { perf ->
        // Hide archived courses from grade sheet (matching Flutter)
        state.courses.none { it.id == perf.id && it.isArchived }
    }

    if (items.isEmpty()) {
        EmptyState(text = "Нет данных по ведомости")
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
                    component.onIntent(
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

@Composable
private fun GradeSheetTile(
    performance: StudentPerformanceCourse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradeColor = gradeColor(performance.total)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Grade badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradeColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = performance.total.toString(),
                color = gradeColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stripEmojiPrefix(performance.name),
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = gradeDescription(performance.total),
                color = gradeColor,
                fontSize = 12.sp,
            )
        }

        Text(
            text = "\u203A", // ›
            color = AppColors.TextSecondary,
            fontSize = 20.sp,
        )
    }
}

// endregion

// region Segment 2: Record Book (Зачетка)

@Composable
private fun GradebookContent(
    state: CoursesComponent.State,
    modifier: Modifier = Modifier,
) {
    val gradebook = state.gradebook
    if (gradebook == null || gradebook.semesters.isEmpty()) {
        EmptyState(text = "Нет данных по зачетке")
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
            .background(AppColors.Surface)
            .padding(12.dp),
    ) {
        Text(
            text = semester.title,
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Regular grades
        semester.regularGrades.forEach { grade ->
            GradeRow(grade = grade)
        }

        // Elective grades
        if (semester.electiveGrades.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Элективы",
                color = AppColors.TextSecondary,
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
                color = AppColors.TextPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = assessmentTypeLabel(grade.assessmentType),
                color = AppColors.TextSecondary,
                fontSize = 11.sp,
            )
        }

        // Grade badge
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

// endregion

// region Shared states

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
            Text(text = "\u26A0\uFE0F", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = AppColors.Error, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text(text = "Повторить", color = AppColors.Accent)
            }
        }
    }
}

@Composable
private fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = AppColors.TextSecondary, fontSize = 14.sp)
    }
}

// endregion

// region Helper functions

/** Grade color by total score (matching Flutter). */
private fun gradeColor(total: Int): Color =
    when {
        total >= 8 -> Color(0xFF66BB6A) // green
        total >= 6 -> Color(0xFFFFEE58) // yellow
        total >= 4 -> Color(0xFFFFA726) // orange
        else -> Color(0xFFEF5350) // red
    }

/** Grade description by total score. */
private fun gradeDescription(total: Int): String =
    when {
        total >= 8 -> "Отлично"
        total >= 6 -> "Хорошо"
        total >= 4 -> "Удовлетворительно"
        else -> "Неудовлетворительно"
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

// endregion
