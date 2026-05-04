package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class GradebookUi(
    val semesters: ImmutableList<GradebookSemesterUi>,
)

data class GradebookSemesterUi(
    val year: Int,
    val semesterNumber: Int,
    val grades: ImmutableList<GradebookGradeUi>,
)

data class GradebookGradeUi(
    val subject: String,
    val normalizedGrade: String,
    val assessmentType: String,
    val subjectType: String,
)
