package io.github.kroune.cumobile.domain.model

data class FileRenameRuleDomain(
    val courseId: String,
    val activityName: String,
    val extension: String,
    val template: String,
)

/**
 * Renders a [FileRenameRuleDomain.template] by substituting placeholders
 * with the given values and replacing spaces with underscores.
 */
fun applyRenameTemplate(
    rule: FileRenameRuleDomain,
    courseName: String,
    activityName: String,
    version: String,
): String =
    rule.template
        .replace("{course}", courseName.replace(" ", "_"))
        .replace("{activity}", activityName.replace(" ", "_"))
        .replace("{version}", version)
        .replace(" ", "_")
