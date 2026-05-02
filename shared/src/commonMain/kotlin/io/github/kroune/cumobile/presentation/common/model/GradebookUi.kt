package io.github.kroune.cumobile.presentation.common.model

data class GradebookUi(
    val semesters: List<GradebookSemesterUi>,
)

data class GradebookSemesterUi(
    val year: Int,
    val semesterNumber: Int,
    val grades: List<GradebookGradeUi>,
)

data class GradebookGradeUi(
    val subject: String,
    val grade: Double?,
    val normalizedGrade: String,
    val assessmentType: String,
    val subjectType: String,
)
