package io.github.kroune.cumobile.presentation.common

import androidx.compose.runtime.Composable

/**
 * A component that can render itself as a Composable.
 *
 * Used by [ChildItems][com.arkivanov.decompose.router.items] children
 * so the LazyColumn can call `item.Render()` without type dispatch.
 */
interface RenderComponent {
    @Composable
    fun Render()
}
