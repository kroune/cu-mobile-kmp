package io.github.kroune.cumobile.presentation.auth.webview

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.kroune.cumobile.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultWebViewLoginComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val onLoginSuccess: () -> Unit,
    private val onBack: () -> Unit,
) : WebViewLoginComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onDestroy() {
                    scope.cancel()
                }
            },
        )
    }

    private val _state = MutableValue(WebViewLoginComponent.State())
    override val state: Value<WebViewLoginComponent.State> = _state

    override fun onIntent(intent: WebViewLoginComponent.Intent) {
        when (intent) {
            is WebViewLoginComponent.Intent.CookieCaptured -> handleCookieCaptured(intent.cookie)
            is WebViewLoginComponent.Intent.BackClicked -> onBack()
        }
    }

    private fun handleCookieCaptured(cookie: String) {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            authRepository.saveCookie(cookie)
            val isValid = authRepository.validateCookie()
            if (isValid) {
                onLoginSuccess()
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось авторизоваться. Попробуйте ещё раз.",
                )
            }
        }
    }
}
