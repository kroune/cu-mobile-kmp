package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Gradebook response containing semester grades.
 *
 * API endpoint: `GET /micro-lms/gradebook`
 */
@Serializable
data class GradebookResponse(
    val semesters: List<GradebookSemester> = emptyList(),
)

/** A single semester in the [GradebookResponse]. */
@Serializable
data class GradebookSemester(
    val year: Int,
    val semesterNumber: Int,
    val grades: List<GradebookGrade> = emptyList(),
) {
    /** Human-readable title, e.g. "2024/2025, семестр 1". */
    val title: String
        get() = "$year/${year + 1}, семестр $semesterNumber"

    /** Non-elective grades. */
    val regularGrades: List<GradebookGrade>
        get() = grades.filter { !it.isElective }

    /** Elective grades. */
    val electiveGrades: List<GradebookGrade>
        get() = grades.filter { it.isElective }
}

/**
 * Individual grade entry in a [GradebookSemester].
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
data class GradebookGrade(
    val subject: String,
    val grade: Double? = null,
    val normalizedGrade: String = "unknown",
    val assessmentType: String = "",
    val subjectType: String = "",
) {
    /** Whether this is an elective subject. */
    val isElective: Boolean
        get() = subjectType == "elective"
}
