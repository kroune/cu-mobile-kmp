package io.github.kroune.cumobile.presentation.common.model

data class NotificationUi(
    val id: String,
    val title: String,
    val description: String,
    val createdAtFormatted: String,
    val icon: String,
    val category: String,
    val linkUri: String?,
    val linkLabel: String?,
)
