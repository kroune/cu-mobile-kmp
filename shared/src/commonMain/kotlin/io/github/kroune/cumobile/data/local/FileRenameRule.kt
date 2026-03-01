package io.github.kroune.cumobile.data.local

import kotlinx.serialization.Serializable

/**
 * A rule for automatically renaming files based on context.
 */
@Serializable
data class FileRenameRule(
    val courseId: Int,
    val activityName: String, // e.g., "Домашнее задание"
    val extension: String,    // e.g., "pdf"
    val template: String,      // e.g., "HW_{course}_{student}_{date}.pdf"
) {
    /**
     * Applies the template using the provided context.
     *
     * Placeholders:
     * - {course}: [courseName]
     * - {activity}: [activityName]
     * - {version}: [version]
     */
    fun apply(
        courseName: String,
        activityName: String,
        version: String,
    ): String {
        return template
            .replace("{course}", courseName.replace(" ", "_"))
            .replace("{activity}", activityName.replace(" ", "_"))
            .replace("{version}", version)
            .replace(" ", "_")
    }
}
