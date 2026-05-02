package io.github.kroune.cumobile.presentation.common.model

data class TaskUi(
    val id: String,
    val courseId: String,
    val themeId: String,
    val longreadId: String,
    val exerciseName: String,
    val courseName: String,
    val statusLabel: String,
    val deadlineBadgeLabel: String,
    val statusStyle: StatusStyle,
    val deadlineFormatted: String?,
    val deadlineTimeFormatted: String,
    val deadlineDayMonthFormatted: String,
    val isOverdue: Boolean,
    val urgencyLevel: UrgencyLevel,
    val lateDaysText: String?,
    val scoreText: String?,
    val score: Double?,
)
