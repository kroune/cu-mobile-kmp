package io.github.kroune.cumobile.domain.model

data class FileRenameRuleDomain(
    val courseId: String,
    val activityName: String,
    val extension: String,
    val template: String,
) {
    fun apply(
        courseName: String,
        activityName: String,
        version: String,
    ): String =
        template
            .replace("{course}", courseName.replace(" ", "_"))
            .replace("{activity}", activityName.replace(" ", "_"))
            .replace("{version}", version)
            .replace(" ", "_")
}
