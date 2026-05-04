package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Comment on a task.
 */
@Serializable
data class TaskCommentApi(
    val id: String = "",
    val content: String = "",
    val sender: CommentSenderApi = CommentSenderApi(),
    /** ISO 8601 datetime string. */
    val createdAt: String? = null,
    val attachments: List<MaterialAttachmentApi> = emptyList(),
    val isEditable: Boolean = false,
    val isDeletable: Boolean = false,
)

/** Author of a [TaskCommentApi]. */
@Serializable
data class CommentSenderApi(
    val id: String = "",
    val email: String = "",
    val name: String = "",
)
