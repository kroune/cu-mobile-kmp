package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class TaskCommentUi(
    val id: String,
    val content: String,
    val senderName: String,
    val senderEmail: String,
    val createdAtFormatted: String?,
    val attachments: ImmutableList<MaterialAttachmentUi>,
    val isEditable: Boolean,
    val isDeletable: Boolean,
)
