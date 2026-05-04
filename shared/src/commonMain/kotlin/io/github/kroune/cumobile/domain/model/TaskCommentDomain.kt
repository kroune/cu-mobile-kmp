package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class TaskCommentDomain(
    val id: String,
    val content: String,
    val sender: CommentSenderDomain,
    val createdAt: Instant?,
    val attachments: List<MaterialAttachmentDomain>,
    val isEditable: Boolean,
    val isDeletable: Boolean,
)

data class CommentSenderDomain(
    val id: String,
    val email: String,
    val name: String,
)
