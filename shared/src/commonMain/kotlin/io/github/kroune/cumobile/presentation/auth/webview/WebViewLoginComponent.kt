package io.github.kroune.cumobile.presentation.auth.webview

import com.arkivanov.decompose.value.Value

/**
 * MVI component for the WebView-based login flow.
 * Displays a WebView loading the CU authentication page
 * and captures the session cookie after successful login.
 */
interface WebViewLoginComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class CookieCaptured(
            val cookie: String,
        ) : Intent

        data object BackClicked : Intent
    }
}
