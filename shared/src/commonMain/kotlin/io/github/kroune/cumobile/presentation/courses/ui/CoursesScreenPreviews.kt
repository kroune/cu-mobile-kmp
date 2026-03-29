package io.github.kroune.cumobile.presentation.courses.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.GradebookGrade
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.GradebookSemester
import io.github.kroune.cumobile.data.model.StudentPerformanceCourse
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.courses.CoursesComponent

private val previewCoursesState = CoursesComponent.State(
    courses = ContentState.Success(
        listOf(
            Course(id = "1", name = "Алгоритмы и структуры данных", category = "development"),
            Course(id = "2", name = "Линейная алгебра", category = "mathematics"),
            Course(id = "3", name = "Управление проектами", category = "business"),
            Course(id = "4", name = "Физика", category = "stem"),
        ),
    ),
    performanceCourses = ContentState.Success(emptyList()),
    gradebook = ContentState.Success(null),
)

@Preview
@Composable
private fun PreviewCoursesScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(horizontal = 16.dp),
        ) {
            CoursesScreenSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewCoursesScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(horizontal = 16.dp),
        ) {
            CoursesScreenSkeleton()
        }
    }
}

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

@Preview
@Composable
private fun PreviewCoursesLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(
            state = CoursesComponent.State(),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCoursesErrorDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(
            state = CoursesComponent.State(
                courses = ContentState.Error("Не удалось загрузить курсы"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCoursesErrorLight() {
    CuMobileTheme(darkTheme = false) {
        CoursesScreenContent(
            state = CoursesComponent.State(
                courses = ContentState.Error("Не удалось загрузить курсы"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewCoursesEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(
            state = CoursesComponent.State(
                courses = ContentState.Success(emptyList()),
                performanceCourses = ContentState.Success(emptyList()),
                gradebook = ContentState.Success(null),
            ),
            onIntent = {},
        )
    }
}

private val previewCoursesWithArchived = previewCoursesState.copy(
    courses = ContentState.Success(
        previewCoursesState.courseList + listOf(
            Course(id = "5", name = "Введение в ИИ", category = "development", isArchived = true),
            Course(id = "6", name = "Философия", category = "general", isArchived = true),
        ),
    ),
    showArchived = true,
)

@Preview
@Composable
private fun PreviewCoursesArchivedDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(state = previewCoursesWithArchived, onIntent = {})
    }
}

private val previewGradeSheetState = CoursesComponent.State(
    segment = 1,
    courses = previewCoursesState.courses,
    performanceCourses = ContentState.Success(
        listOf(
            StudentPerformanceCourse(id = "1", name = "Алгоритмы и структуры данных", total = 8),
            StudentPerformanceCourse(id = "2", name = "Линейная алгебра", total = 6),
            StudentPerformanceCourse(id = "3", name = "Управление проектами", total = 4),
        ),
    ),
    gradebook = ContentState.Success(null),
)

@Preview
@Composable
private fun PreviewGradeSheetDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(state = previewGradeSheetState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewGradeSheetLight() {
    CuMobileTheme(darkTheme = false) {
        CoursesScreenContent(state = previewGradeSheetState, onIntent = {})
    }
}

private val previewGradebookState = CoursesComponent.State(
    segment = 2,
    courses = ContentState.Success(emptyList()),
    performanceCourses = ContentState.Success(emptyList()),
    gradebook = ContentState.Success(
        GradebookResponse(
            semesters = listOf(
                GradebookSemester(
                    year = 2025,
                    semesterNumber = 1,
                    grades = listOf(
                        GradebookGrade(
                            subject = "Математический анализ",
                            grade = 5.0,
                            normalizedGrade = "excellent",
                            assessmentType = "exam",
                        ),
                        GradebookGrade(
                            subject = "Физическая культура",
                            normalizedGrade = "passed",
                            assessmentType = "credit",
                        ),
                        GradebookGrade(
                            subject = "Основы программирования",
                            grade = 4.0,
                            normalizedGrade = "good",
                            assessmentType = "difCredit",
                            subjectType = "elective",
                        ),
                    ),
                ),
            ),
        ),
    ),
)

@Preview
@Composable
private fun PreviewGradebookDark() {
    CuMobileTheme(darkTheme = true) {
        CoursesScreenContent(state = previewGradebookState, onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewGradebookLight() {
    CuMobileTheme(darkTheme = false) {
        CoursesScreenContent(state = previewGradebookState, onIntent = {})
    }
}
