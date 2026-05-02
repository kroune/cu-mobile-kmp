package io.github.kroune.cumobile.presentation.files.rename

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.model.CourseUi
import io.github.kroune.cumobile.presentation.common.model.FileRenameRuleUi

/**
 * MVI component for managing file renaming templates.
 */
interface FileRenameSettingsComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val rules: List<FileRenameRuleUi> = emptyList(),
        val courses: List<CourseUi> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class AddRule(
            val rule: FileRenameRuleUi,
        ) : Intent

        data class DeleteRule(
            val rule: FileRenameRuleUi,
        ) : Intent

        data object Back : Intent

        data object Refresh : Intent
    }
}
