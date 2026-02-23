package io.github.kroune.cumobile.presentation.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default implementation of [LoginComponent].
 *
 * Displays the login screen with a "Войти" button that
 * navigates to the WebView-based authentication flow.
 */
class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val onNavigateToWebView: () -> Unit,
) : LoginComponent,
    ComponentContext by componentContext {
    private val _state = MutableValue(LoginComponent.State())
    override val state: Value<LoginComponent.State> = _state

    override fun onIntent(intent: LoginComponent.Intent) {
        when (intent) {
            is LoginComponent.Intent.LoginClicked -> onNavigateToWebView()
        }
    }
}
