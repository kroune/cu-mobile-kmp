package io.github.kroune.cumobile

import androidx.compose.ui.window.ComposeUIViewController
import io.github.kroune.cumobile.presentation.root.RootComponent

fun rootViewController(root: RootComponent) {
    ComposeUIViewController { App(root) }
}
