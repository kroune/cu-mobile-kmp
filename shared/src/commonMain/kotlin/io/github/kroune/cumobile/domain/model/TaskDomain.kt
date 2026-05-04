package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

enum class TaskStatus {
    Backlog,
    InProgress,
    HasSolution,
    Review,
    Revision,
    Rework,
    Failed,
    Rejected,
    Evaluated,
    Unknown,
    ;

    val isActive: Boolean get() = this in ACTIVE_STATUSES
    val isArchived: Boolean get() = !isActive

    companion object {
        val ACTIVE_STATUSES: Set<TaskStatus> = linkedSetOf(
            Backlog,
            InProgress,
            HasSolution,
            Review,
            Revision,
            Rework,
        )

        val ARCHIVE_STATUSES: Set<TaskStatus> = linkedSetOf(
            Evaluated,
            Failed,
            Rejected,
        )
    }
}

data class TaskDomain(
    val id: String,
    val status: TaskStatus,
    val score: Double?,
    val extraScore: Double?,
    val deadline: Instant?,
    val submitAt: Instant?,
    val startedAt: Instant?,
    val exerciseId: String,
    val exerciseName: String,
    val exerciseType: String,
    val exerciseMaxScore: Int,
    val exerciseDeadline: Instant?,
    val activityName: String?,
    val activityWeight: Double?,
    val courseId: String,
    val courseName: String,
    val courseIsArchived: Boolean,
    val themeId: String,
    val themeName: String,
    val longreadId: String,
    val isLateDaysEnabled: Boolean,
    val lateDays: Int?,
)
