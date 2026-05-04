package io.github.kroune.cumobile.presentation.common.model

data class CourseUi(
    val id: String,
    val name: String,
    val isArchived: Boolean,
    val categoryLabel: String,
    val categoryStyle: CategoryStyle,
)
