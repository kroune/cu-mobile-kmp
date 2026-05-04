package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.formatDeadlineDayShortMonth
import io.github.kroune.cumobile.presentation.common.formatDeadlineInstant
import io.github.kroune.cumobile.presentation.common.formatDeadlineTime
import io.github.kroune.cumobile.presentation.common.isOverdue
import io.github.kroune.cumobile.presentation.common.model.TaskUi
import io.github.kroune.cumobile.presentation.common.model.UrgencyLevel
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle
import io.github.kroune.cumobile.presentation.common.ui.stripEmojiPrefix
import kotlin.time.Instant

private const val MillisPerHour = 3_600_000L
private const val MillisPerDay = 86_400_000L
private const val HoursPerDay = 24
private const val UrgencyRedHours = 24L
private const val UrgencyOrangeHours = 72L

fun TaskDomain.toUi(now: Instant): TaskUi {
    val statusStyle = status.toStatusStyle()
    val deadline = deadline ?: exerciseDeadline
    return TaskUi(
        id = id,
        courseId = courseId,
        themeId = themeId,
        longreadId = longreadId,
        exerciseName = stripEmojiPrefix(exerciseName),
        courseName = stripEmojiPrefix(courseName),
        statusLabel = statusStyle.label(),
        deadlineBadgeLabel = deadlineBadgeLabel(status, score),
        statusStyle = statusStyle,
        deadlineFormatted = deadline?.let { formatDeadlineInstant(it) },
        deadlineTimeFormatted = formatDeadlineTime(exerciseDeadline),
        deadlineDayMonthFormatted = formatDeadlineDayShortMonth(exerciseDeadline),
        isOverdue = isOverdue(deadline, now),
        urgencyLevel = computeUrgencyLevel(exerciseDeadline, now),
        lateDaysText = formatLateDays(isLateDaysEnabled, lateDays),
        scoreText = score?.let { "${it.toInt()}" },
    )
}

private fun deadlineBadgeLabel(
    status: TaskStatus,
    score: Double?,
): String =
    if (status == TaskStatus.Evaluated && score != null) {
        "${score.toInt()}"
    } else {
        status.toStatusStyle().label()
    }

private fun computeUrgencyLevel(
    deadline: Instant?,
    now: Instant,
): UrgencyLevel {
    if (deadline == null) return UrgencyLevel.Normal
    val diffMs = deadline.toEpochMilliseconds() - now.toEpochMilliseconds()
    if (diffMs <= 0) return UrgencyLevel.Red
    val totalHours = diffMs / MillisPerHour
    val totalDaysCeil = (diffMs + MillisPerDay - 1) / MillisPerDay
    return when {
        totalHours < UrgencyRedHours -> UrgencyLevel.Red
        totalDaysCeil <= UrgencyOrangeHours / HoursPerDay -> UrgencyLevel.Orange
        else -> UrgencyLevel.Normal
    }
}

private fun formatLateDays(
    isEnabled: Boolean,
    lateDays: Int?,
): String? {
    if (!isEnabled || lateDays == null || lateDays <= 0) return null
    return "Перенесено на $lateDays дн."
}
