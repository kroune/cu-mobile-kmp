package io.github.kroune.cumobile.presentation.courses.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.CategoryStyle
import io.github.kroune.cumobile.presentation.common.model.CourseGradeUi
import io.github.kroune.cumobile.presentation.common.model.CourseUi
import io.github.kroune.cumobile.presentation.common.model.GradebookGradeUi
import io.github.kroune.cumobile.presentation.common.model.GradebookSemesterUi
import io.github.kroune.cumobile.presentation.common.model.GradebookUi
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.courses.CoursesComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val previewCoursesList = persistentListOf(
    CourseUi(
        id = "1",
        name = "Алгоритмы и структуры данных",
        isArchived = false,
        categoryLabel = CategoryStyle.Development.label(),
        categoryStyle = CategoryStyle.Development,
    ),
    CourseUi(
        id = "2",
        name = "Линейная алгебра",
        isArchived = false,
        categoryLabel = CategoryStyle.Mathematics.label(),
        categoryStyle = CategoryStyle.Mathematics,
    ),
    CourseUi(
        id = "3",
        name = "Управление проектами",
        isArchived = false,
        categoryLabel = CategoryStyle.Business.label(),
        categoryStyle = CategoryStyle.Business,
    ),
    CourseUi(
        id = "4",
        name = "Физика",
        isArchived = false,
        categoryLabel = CategoryStyle.Stem.label(),
        categoryStyle = CategoryStyle.Stem,
    ),
)

private val previewCoursesState = CoursesComponent.State(
    courses = ContentState.Success(previewCoursesList),
    performanceCourses = ContentState.Success(persistentListOf()),
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
                courses = ContentState.Success(persistentListOf()),
                performanceCourses = ContentState.Success(persistentListOf()),
                gradebook = ContentState.Success(null),
            ),
            onIntent = {},
        )
    }
}

private val previewArchivedCourses = persistentListOf(
    CourseUi(
        id = "5",
        name = "Введение в ИИ",
        isArchived = true,
        categoryLabel = CategoryStyle.Development.label(),
        categoryStyle = CategoryStyle.Development,
    ),
    CourseUi(
        id = "6",
        name = "Философия",
        isArchived = true,
        categoryLabel = CategoryStyle.General.label(),
        categoryStyle = CategoryStyle.General,
    ),
)

private val previewCoursesWithArchived = previewCoursesState.copy(
    courses = ContentState.Success((previewCoursesList + previewArchivedCourses).toPersistentList()),
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
        persistentListOf(
            CourseGradeUi(id = "1", name = "Алгоритмы и структуры данных", description = null, total = 8),
            CourseGradeUi(id = "2", name = "Линейная алгебра", description = null, total = 6),
            CourseGradeUi(id = "3", name = "Управление проектами", description = null, total = 4),
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
    courses = ContentState.Success(persistentListOf()),
    performanceCourses = ContentState.Success(persistentListOf()),
    gradebook = ContentState.Success(
        GradebookUi(
            semesters = listOf(
                GradebookSemesterUi(
                    year = 2025,
                    semesterNumber = 1,
                    grades = listOf(
                        GradebookGradeUi(
                            subject = "Математический анализ",
                            grade = 5.0,
                            normalizedGrade = "excellent",
                            assessmentType = "exam",
                            subjectType = "regular",
                        ),
                        GradebookGradeUi(
                            subject = "Физическая культура",
                            grade = null,
                            normalizedGrade = "passed",
                            assessmentType = "credit",
                            subjectType = "regular",
                        ),
                        GradebookGradeUi(
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
