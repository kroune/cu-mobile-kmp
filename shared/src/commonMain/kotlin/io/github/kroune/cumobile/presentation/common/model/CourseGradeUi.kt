package io.github.kroune.cumobile.presentation.common.model

data class CourseGradeUi(
    val id: String,
    val name: String,
    val description: String?,
    val total: Int,
    /** Pre-formatted total for display. */
    val totalFormatted: String,
    /** Human-readable grade description (e.g. "Отлично"). */
    val totalDescription: String,
)
