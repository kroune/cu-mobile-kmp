package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class NotificationDomain(
    val id: String,
    val notificationId: String?,
    val createdAt: Instant?,
    val category: String,
    val icon: String,
    val title: String,
    val description: String,
    val linkUri: String?,
    val linkLabel: String?,
    val linkTarget: String?,
)
