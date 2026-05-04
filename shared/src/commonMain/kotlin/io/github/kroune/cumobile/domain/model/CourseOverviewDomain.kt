package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class CourseOverviewDomain(
    val id: String,
    val name: String,
    val isArchived: Boolean,
    val themes: List<CourseThemeDomain>,
)

data class CourseThemeDomain(
    val id: String,
    val name: String,
    val order: Int,
    val state: String,
    val longreads: List<LongreadDomain>,
)

data class LongreadDomain(
    val id: String,
    val type: String,
    val name: String,
    val state: String,
    val exercises: List<ThemeExerciseDomain>,
)

data class ThemeExerciseDomain(
    val id: String,
    val name: String,
    val maxScore: Int,
    val deadline: Instant?,
    val activityName: String?,
    val activityWeight: Double?,
)
