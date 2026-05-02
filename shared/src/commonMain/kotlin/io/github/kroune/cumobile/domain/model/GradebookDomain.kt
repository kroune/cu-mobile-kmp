package io.github.kroune.cumobile.domain.model

data class GradebookResponseDomain(
    val semesters: List<GradebookSemesterDomain>,
)

data class GradebookSemesterDomain(
    val year: Int,
    val semesterNumber: Int,
    val grades: List<GradebookGradeDomain>,
)

data class GradebookGradeDomain(
    val subject: String,
    val grade: Double?,
    val normalizedGrade: String,
    val assessmentType: String,
    val subjectType: String,
)
