package io.github.kroune.cumobile.presentation.auth

import com.arkivanov.decompose.value.Value

/**
 * Component managing the login screen.
 * Displays a login button that launches the WebView-based authentication flow.
 */
interface LoginComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        /** User tapped the login button. */
        data object LoginClicked : Intent
    }
}
