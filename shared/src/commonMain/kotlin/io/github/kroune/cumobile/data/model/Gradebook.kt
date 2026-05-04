package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Gradebook response containing semester grades.
 */
@Serializable
data class GradebookResponseApi(
    val semesters: List<GradebookSemesterApi> = emptyList(),
)

/** A single semester in the [GradebookResponseApi]. */
@Serializable
data class GradebookSemesterApi(
    val year: Int,
    val semesterNumber: Int,
    val grades: List<GradebookGradeApi> = emptyList(),
) {
    /** Non-elective grades. */
    val regularGrades: List<GradebookGradeApi>
        get() = grades.filter { !it.isElective }

    /** Elective grades. */
    val electiveGrades: List<GradebookGradeApi>
        get() = grades.filter { it.isElective }
}

/**
 * Individual grade entry in a [GradebookSemesterApi].
 *
 * Known [normalizedGrade] values:
 * `"passed"` (Зачтено), `"excellent"` (Отлично), `"good"` (Хорошо),
 * `"satisfactory"` (Удовл.), `"failed"` (Не сдано), `"unknown"`.
 *
 * Known [assessmentType] values:
 * `"exam"` (Экзамен), `"credit"` (Зачет), `"difCredit"` (Дифф. зачет).
 *
 * Known [subjectType] values: `"elective"` vs others.
 */
@Serializable
data class GradebookGradeApi(
    val subject: String,
    val grade: Double? = null,
    val normalizedGrade: String = "unknown",
    val assessmentType: String = "",
    val subjectType: String = "",
) {
    /** Whether this is an elective subject. */
    val isElective: Boolean
        get() = subjectType == SubjectType.Elective

    /** Known [subjectType] values. */
    object SubjectType {
        const val Elective = "elective"
    }
}
