package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class LongreadMaterialUi(
    val id: String,
    val discriminator: String,
    val viewContent: String?,
    val filename: String?,
    val version: String?,
    val length: Int?,
    val name: String?,
    val contentName: String?,
    val attachments: ImmutableList<MaterialAttachmentUi>,
    val deadlineFormatted: String?,
    val maxScore: Int?,
    val activityName: String?,
    val activityWeight: Double?,
    val taskId: String?,
)
