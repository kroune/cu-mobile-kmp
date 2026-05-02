package io.github.kroune.cumobile.presentation.common.model

data class CourseOverviewUi(
    val id: String,
    val name: String,
    val isArchived: Boolean,
    val themes: List<CourseThemeUi>,
)

data class CourseThemeUi(
    val id: String,
    val name: String,
    val order: Int,
    val state: String,
    val longreads: List<LongreadSummaryUi>,
)

data class LongreadSummaryUi(
    val id: String,
    val type: String,
    val name: String,
    val state: String,
    val exercises: List<ThemeExerciseUi>,
)

data class ThemeExerciseUi(
    val id: String,
    val name: String,
    val maxScore: Int,
    val deadlineFormatted: String?,
    val activityName: String?,
)
