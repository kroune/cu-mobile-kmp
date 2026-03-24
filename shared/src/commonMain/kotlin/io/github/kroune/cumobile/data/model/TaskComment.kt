package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Comment on a task.
 */
@Serializable
data class TaskComment(
    val id: String = "",
    val content: String = "",
    val sender: CommentSender = CommentSender(),
    /** ISO 8601 datetime string. */
    val createdAt: String? = null,
    val attachments: List<MaterialAttachment> = emptyList(),
    val isEditable: Boolean = false,
    val isDeletable: Boolean = false,
)

/** Author of a [TaskComment]. */
@Serializable
data class CommentSender(
    val id: String = "",
    val email: String = "",
    val name: String = "",
)
