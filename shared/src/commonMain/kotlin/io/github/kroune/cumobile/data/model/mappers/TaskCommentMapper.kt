package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.TaskCommentApi
import io.github.kroune.cumobile.domain.model.CommentSenderDomain
import io.github.kroune.cumobile.domain.model.TaskCommentDomain

fun TaskCommentApi.toDomain(): TaskCommentDomain =
    TaskCommentDomain(
        id = id,
        content = content,
        sender = CommentSenderDomain(
            id = sender.id,
            email = sender.email,
            name = sender.name,
        ),
        createdAt = parseInstant(createdAt),
        attachments = attachments.map { it.toDomain() },
        isEditable = isEditable,
        isDeletable = isDeletable,
    )
