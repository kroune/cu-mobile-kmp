package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.NotificationDomain
import io.github.kroune.cumobile.presentation.common.formatDateTimeFullInstant
import io.github.kroune.cumobile.presentation.common.model.NotificationUi

fun NotificationDomain.toUi(): NotificationUi =
    NotificationUi(
        id = id,
        title = title,
        description = description,
        createdAtFormatted = formatDateTimeFullInstant(createdAt),
        icon = icon,
        category = category,
        linkUri = linkUri,
        linkLabel = linkLabel,
    )
