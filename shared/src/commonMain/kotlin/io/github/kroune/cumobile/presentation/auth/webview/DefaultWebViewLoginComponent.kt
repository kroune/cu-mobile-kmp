package io.github.kroune.cumobile.presentation.auth.webview

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.CookieValidationResult
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class DefaultWebViewLoginComponent(
    componentContext: ComponentContext,
    authRepository: Lazy<AuthRepository>,
    private val onLoginSuccess: () -> Unit,
    private val onBack: () -> Unit,
) : WebViewLoginComponent,
    ComponentContext by componentContext {
    private val authRepository by authRepository

    private val scope = componentScope()

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
        logger.info { "Cookie captured, length=${cookie.length}" }
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            logger.debug { "Saving cookie..." }
            authRepository.saveCookie(cookie)
            logger.debug { "Cookie saved, validating..." }
            val result = authRepository.validateCookie()
            logger.info { "Cookie validation result: $result" }
            when (result) {
                CookieValidationResult.Valid -> onLoginSuccess()
                CookieValidationResult.Invalid -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось авторизоваться. Попробуйте ещё раз.",
                )
                CookieValidationResult.NetworkError -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка сети. Проверьте подключение и попробуйте снова.",
                )
            }
        }
    }
}
