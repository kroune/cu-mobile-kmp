package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.LongreadMaterialApi
import io.github.kroune.cumobile.domain.model.LongreadMaterialDomain

fun LongreadMaterialApi.toDomain(): LongreadMaterialDomain =
    LongreadMaterialDomain(
        id = id,
        discriminator = discriminator,
        viewContentRaw = viewContentRaw?.toString(),
        filename = filename,
        version = version,
        length = length,
        name = name,
        contentName = content?.name,
        attachments = attachments.map { it.toDomain() },
        estimationDeadline = parseDeadlineInstant(estimation?.deadline),
        estimationMaxScore = estimation?.maxScore,
        estimationActivityName = estimation?.activity?.name,
        estimationActivityWeight = estimation?.activity?.weight,
        taskId = taskId,
    )
