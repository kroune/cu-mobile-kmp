package io.github.kroune.cumobile.presentation.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

enum class StatusStyle {
    Backlog,
    InProgress,
    HasSolution,
    Review,
    Revision,
    Rework,
    Failed,
    Rejected,
    Evaluated,
    ;

    companion object {
        private val byApiValue = mapOf(
            "backlog" to Backlog,
            "inProgress" to InProgress,
            "hasSolution" to HasSolution,
            "review" to Review,
            "revision" to Revision,
            "rework" to Rework,
            "failed" to Failed,
            "rejected" to Rejected,
            "evaluated" to Evaluated,
        )

        fun fromApiValue(value: String): StatusStyle =
            byApiValue[value] ?: Backlog
    }
}

fun StatusStyle.label(): String =
    when (this) {
        StatusStyle.Backlog -> "Не начато"
        StatusStyle.InProgress -> "В работе"
        StatusStyle.HasSolution -> "Есть решение"
        StatusStyle.Review -> "На проверке"
        StatusStyle.Revision -> "Доработка"
        StatusStyle.Rework -> "Доработка"
        StatusStyle.Failed -> "Не сдано"
        StatusStyle.Rejected -> "Не сдано"
        StatusStyle.Evaluated -> "Проверено"
    }

@Composable
@ReadOnlyComposable
fun StatusStyle.color(): Color {
    val colors = AppTheme.colors
    return when (this) {
        StatusStyle.Backlog -> colors.taskBacklog
        StatusStyle.InProgress -> colors.taskInProgress
        StatusStyle.HasSolution -> colors.taskHasSolution
        StatusStyle.Review -> colors.taskReview
        StatusStyle.Revision -> colors.taskRevision
        StatusStyle.Rework -> colors.taskRework
        StatusStyle.Failed -> colors.taskFailed
        StatusStyle.Rejected -> colors.taskFailed
        StatusStyle.Evaluated -> colors.taskEvaluated
    }
}

fun TaskStatus.toStatusStyle(): StatusStyle =
    when (this) {
        TaskStatus.Backlog -> StatusStyle.Backlog
        TaskStatus.InProgress -> StatusStyle.InProgress
        TaskStatus.HasSolution -> StatusStyle.HasSolution
        TaskStatus.Review -> StatusStyle.Review
        TaskStatus.Revision -> StatusStyle.Revision
        TaskStatus.Rework -> StatusStyle.Rework
        TaskStatus.Failed -> StatusStyle.Failed
        TaskStatus.Rejected -> StatusStyle.Rejected
        TaskStatus.Evaluated -> StatusStyle.Evaluated
        TaskStatus.Unknown -> StatusStyle.Backlog
    }
