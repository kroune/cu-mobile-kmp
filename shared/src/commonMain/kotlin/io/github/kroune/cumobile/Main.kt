package io.github.kroune.cumobile

import androidx.compose.runtime.Composable
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.root.RootComponent
import io.github.kroune.cumobile.presentation.root.RootScreen

/**
 * App entry point composable.
 * Renders the [RootScreen] which handles auth routing via Decompose.
 */
@Composable
fun App(rootComponent: RootComponent) {
    CuMobileTheme {
        RootScreen(rootComponent)
    }
}
