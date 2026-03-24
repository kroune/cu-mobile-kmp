package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Course summary from the student course list.
 *
 * Known [category] values: `"mathematics"`, `"development"`, `"stem"`,
 * `"general"`, `"business"`, `"softSkills"`, `"withoutCategory"`.
 *
 * Known [state] values: `"active"`, `"archived"`.
 */
@Serializable
data class Course(
    val id: String = "",
    val name: String = "",
    val state: String = "",
    val category: String = "general",
    val categoryCover: String = "",
    val isArchived: Boolean = false,
)
