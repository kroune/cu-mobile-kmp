package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.NotificationApi
import io.github.kroune.cumobile.domain.model.NotificationDomain

fun NotificationApi.toDomain(): NotificationDomain =
    NotificationDomain(
        id = id,
        notificationId = notificationId,
        createdAt = parseInstant(createdAt.ifEmpty { null }),
        category = category,
        icon = icon,
        title = title,
        description = description,
        linkUri = link?.uri,
        linkLabel = link?.label,
        linkTarget = link?.target,
    )
