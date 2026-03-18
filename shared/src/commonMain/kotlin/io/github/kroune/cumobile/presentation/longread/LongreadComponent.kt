package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent

/**
 * MVI component for the longread/material viewer screen.
 *
 * Displays materials within a longread (markdown, file, coding, questions).
 * For coding materials, provides task management: start task, submit
 * solution, comments, events timeline, and late days management.
 */
interface LongreadComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val longreadId: Int = 0,
        val courseId: Int = 0,
        val themeId: Int = 0,
        val materials: List<LongreadMaterial> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Task details keyed by taskId for coding materials. */
        val taskDetails: Map<Int, TaskDetails> = emptyMap(),
        /** Events for the currently selected task. */
        val taskEvents: List<TaskEvent> = emptyList(),
        /** Comments for the currently selected task. */
        val taskComments: List<TaskComment> = emptyList(),
        /** Currently selected task ID (for expanded coding material). */
        val activeTaskId: Int? = null,
        /** Selected tab within the active task: "solution", "comments", "info". */
        val selectedTaskTab: String = "solution",
        /** Solution URL input for the active task. */
        val solutionUrl: String = "",
        /** Comment text input for the active task. */
        val commentText: String = "",
        /** Whether a task action is in progress. */
        val isSubmitting: Boolean = false,
        /** Whether the search bar is visible. */
        val isSearchVisible: Boolean = false,
        /** Current search query text. */
        val searchQuery: String = "",
        /** Total number of search matches across all materials. */
        val searchMatchCount: Int = 0,
        /** Index of the currently focused match (0-based). */
        val currentMatchIndex: Int = 0,
    ) {
        /** Title derived from the first material's name or a fallback. */
        val title: String
            get() = materials.firstOrNull()?.contentName
                ?: materials.firstOrNull()?.name
                ?: "Лонгрид"

        /** Coding materials that have a taskId. */
        val codingMaterials: List<LongreadMaterial>
            get() = materials.filter { it.isCoding && it.taskId != null }
    }

    sealed interface Intent {
        data object Back : Intent

        data object Refresh : Intent

        data class SelectTask(
            val taskId: Int,
        ) : Intent

        data class SelectTaskTab(
            val tab: String,
        ) : Intent

        data class UpdateSolutionUrl(
            val url: String,
        ) : Intent

        data class UpdateCommentText(
            val text: String,
        ) : Intent

        data object StartTask : Intent

        data object SubmitSolution : Intent

        data object CreateComment : Intent

        data class ProlongLateDays(
            val days: Int,
        ) : Intent

        data object CancelLateDays : Intent

        data class DownloadFile(
            val material: LongreadMaterial,
        ) : Intent

        data object ToggleSearch : Intent

        data class UpdateSearchQuery(
            val query: String,
        ) : Intent

        data object NextMatch : Intent

        data object PreviousMatch : Intent
    }
}
