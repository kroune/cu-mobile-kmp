package io.github.kroune.cumobile.presentation.files.rename

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.data.model.Course

/**
 * MVI component for managing file renaming templates.
 */
interface FileRenameSettingsComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val rules: List<FileRenameRule> = emptyList(),
        val courses: List<Course> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class AddRule(
            val rule: FileRenameRule,
        ) : Intent

        data class DeleteRule(
            val rule: FileRenameRule,
        ) : Intent

        data object Back : Intent

        data object Refresh : Intent
    }
}
