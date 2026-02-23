package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Comment on a task.
 *
 * API endpoint: `GET /micro-lms/tasks/{taskId}/comments`
 */
@Serializable
data class TaskComment(
    val id: Int = 0,
    val content: String = "",
    val sender: CommentSender = CommentSender(),
    /** ISO 8601 datetime string. */
    val createdAt: String? = null,
    val attachments: List<MaterialAttachment> = emptyList(),
)

/** Author of a [TaskComment]. */
@Serializable
data class CommentSender(
    val id: String = "",
    val email: String = "",
    val name: String = "",
)
