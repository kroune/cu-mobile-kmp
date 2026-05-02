package io.github.kroune.cumobile.domain.model

enum class CourseCategory(
    val apiValue: String,
) {
    General("general"),
    Mathematics("mathematics"),
    Development("development"),
    Stem("stem"),
    Business("business"),
    SoftSkills("softSkills"),
    Unknown(""),
    ;

    companion object {
        fun fromApi(value: String): CourseCategory =
            entries.find { it.apiValue == value } ?: Unknown
    }
}

data class CourseDomain(
    val id: String,
    val name: String,
    val isArchived: Boolean,
    val category: CourseCategory,
    val categoryCoverUrl: String,
)
