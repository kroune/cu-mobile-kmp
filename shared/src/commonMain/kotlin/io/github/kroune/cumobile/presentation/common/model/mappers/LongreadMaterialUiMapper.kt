package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.LongreadMaterialDomain
import io.github.kroune.cumobile.domain.model.MaterialAttachmentDomain
import io.github.kroune.cumobile.domain.model.viewContent
import io.github.kroune.cumobile.presentation.common.formatDeadlineInstant
import io.github.kroune.cumobile.presentation.common.model.LongreadMaterialUi
import io.github.kroune.cumobile.presentation.common.model.MaterialAttachmentUi
import kotlinx.collections.immutable.toImmutableList

fun MaterialAttachmentDomain.toUi(): MaterialAttachmentUi =
    MaterialAttachmentUi(
        name = name,
        filename = filename,
        mediaType = mediaType,
        length = length,
        version = version,
    )

fun LongreadMaterialDomain.toUi(): LongreadMaterialUi =
    LongreadMaterialUi(
        id = id,
        discriminator = discriminator,
        viewContent = viewContent,
        filename = filename,
        version = version,
        length = length,
        name = name,
        contentName = contentName,
        attachments = attachments.map { it.toUi() }.toImmutableList(),
        deadlineFormatted = formatDeadlineInstant(estimationDeadline),
        maxScore = estimationMaxScore,
        activityName = estimationActivityName,
        activityWeight = estimationActivityWeight,
        taskId = taskId,
    )
