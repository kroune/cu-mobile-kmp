package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.NotificationDomain
import io.github.kroune.cumobile.presentation.common.formatDateTimeFull
import io.github.kroune.cumobile.presentation.common.model.NotificationUi

fun NotificationDomain.toUi(): NotificationUi =
    NotificationUi(
        id = id,
        title = title,
        description = description,
        createdAtFormatted = createdAt?.toString()?.let { formatDateTimeFull(it) }.orEmpty(),
        icon = icon,
        category = category,
        linkUri = linkUri,
        linkLabel = linkLabel,
    )
