package io.github.kroune.cumobile.presentation.longread

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.items.LazyChildItems
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.longread.component.LongreadItem
import io.github.kroune.cumobile.presentation.longread.component.MaterialConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the longread/material viewer screen.
 *
 * Displays materials as ChildItems — each material is its own component
 * with automatic lifecycle management. Task-specific state is owned
 * by individual [CodingMaterialComponent][io.github.kroune.cumobile.presentation.longread.component.coding.CodingMaterialComponent]
 * children.
 */
@OptIn(ExperimentalDecomposeApi::class)
interface LongreadComponent {
    val state: Value<State>
    val effects: Flow<Effect>
    val materialItems: LazyChildItems<MaterialConfig, LongreadItem>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class SnackBarEffect(
            val message: String,
            val actionLabel: String? = null,
            val withDismissAction: Boolean = false,
            val duration: SnackbarDuration =
                if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
            val onSnackbarResult: (SnackbarResult) -> Unit = {},
        ) : Effect
    }

    data class State(
        val longreadId: String = "",
        val courseId: String = "",
        val themeId: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val materials: ImmutableList<LongreadMaterial> = persistentListOf(),
        val title: String = "Лонгрид",
        val isSearchVisible: Boolean = false,
        val searchQuery: String = "",
        val searchMatchCount: Int = 0,
        val currentMatchIndex: Int = 0,
    ) {
    }

    sealed interface Intent {
        sealed interface Navigation : Intent {
            data object Back : Navigation

            data object Refresh : Navigation

            data object NavigateToFiles : Navigation
        }

        sealed interface Search : Intent {
            data object ToggleSearch : Search

            data object NextMatch : Search

            data object PreviousMatch : Search

            data class UpdateSearchQuery(
                val query: String,
            ) : Search
        }
    }
}
