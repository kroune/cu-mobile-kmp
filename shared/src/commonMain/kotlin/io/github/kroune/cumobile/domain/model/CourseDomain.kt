package io.github.kroune.cumobile.domain.model

enum class CourseCategory {
    General,
    Mathematics,
    Development,
    Stem,
    Business,
    SoftSkills,
    Unknown,
}

data class CourseDomain(
    val id: String,
    val name: String,
    val isArchived: Boolean,
    val category: CourseCategory,
    val categoryCoverUrl: String,
)
