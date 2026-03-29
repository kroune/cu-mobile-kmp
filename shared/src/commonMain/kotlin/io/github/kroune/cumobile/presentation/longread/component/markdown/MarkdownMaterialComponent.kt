package io.github.kroune.cumobile.presentation.longread.component.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.longread.component.ExternalUpdate
import io.github.kroune.cumobile.presentation.longread.ui.markdown.MarkdownMaterialCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Simple material component for markdown (HTML) content.
 *
 * Collects [io.github.kroune.cumobile.presentation.longread.component.ExternalUpdate.SearchQuery] events from the parent's shared flow
 * and stores the query in its own state for reactive highlight rendering.
 *
 * [material] is immutable data fixed at creation time — it never changes
 * during the component's lifetime and is intentionally NOT in [State]
 * to avoid bloating every `state.copy()` with a large unchanged object.
 */
class MarkdownMaterialComponent(
    componentContext: ComponentContext,
    private val material: LongreadMaterial,
    externalUpdates: Flow<ExternalUpdate>,
) : ComponentContext by componentContext,
    RenderComponent {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(State())
    val state: Value<State> = _state

    data class State(
        val searchQuery: String = "",
    )

    init {
        scope.launch {
            externalUpdates.collect { update ->
                when (update) {
                    is ExternalUpdate.SearchQuery ->
                        _state.value = _state.value.copy(searchQuery = update.query)
                }
            }
        }
    }

    @Composable
    override fun Render() {
        val currentState by state.subscribeAsState()
        MarkdownMaterialCard(material, currentState)
    }
}
