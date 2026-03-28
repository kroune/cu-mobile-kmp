package io.github.kroune.cumobile.presentation.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.network.AuthApiService
import io.github.kroune.cumobile.data.network.AuthStepResult
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.presentation.auth.LoginComponent.AuthStep
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [LoginComponent].
 *
 * Performs native Keycloak authentication via [AuthApiService].
 * Falls back to WebView if user chooses or if something breaks.
 */
class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val authApiServiceFactory: () -> AuthApiService,
    private val onLoginSuccess: () -> Unit,
    private val onNavigateToWebView: () -> Unit,
) : LoginComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val _state = MutableValue(LoginComponent.State())
    override val state: Value<LoginComponent.State> = _state

    private var authApi: AuthApiService? = null
    private var currentLoginAction: String? = null
    private var currentPhoneNumber: String? = null

    init {
        startAuthFlow()
    }

    override fun onIntent(intent: LoginComponent.Intent) {
        when (intent) {
            is LoginComponent.Intent.UpdateEmail ->
                _state.update { it.copy(email = intent.value) }

            is LoginComponent.Intent.UpdatePassword ->
                _state.update { it.copy(password = intent.value) }

            is LoginComponent.Intent.UpdateOtpCode ->
                _state.update { it.copy(otpCode = intent.value) }

            is LoginComponent.Intent.Submit -> handleSubmit()
            is LoginComponent.Intent.Back -> handleBack()
            is LoginComponent.Intent.FallbackToWebView -> {
                authApi?.close()
                authApi = null
                onNavigateToWebView()
            }
        }
    }

    private fun startAuthFlow() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val api = authApiServiceFactory()
            authApi = api
            when (val result = api.startAuth()) {
                is AuthStepResult.NextStep -> {
                    currentLoginAction = result.loginAction
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = mapActivePage(result.activePage),
                            error = result.errorMessage,
                        )
                    }
                }

                is AuthStepResult.Redirect -> {
                    handleRedirect(result.callbackUrl)
                }

                is AuthStepResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun handleSubmit() {
        val action = currentLoginAction
        if (action == null) {
            logger.warn { "handleSubmit: no loginAction available" }
            _state.update { it.copy(error = "Нет активной сессии, попробуйте снова") }
            return
        }
        val currentState = _state.value
        if (currentState.isLoading) return
        logger.info { "handleSubmit: step=${currentState.step}" }

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val api = authApi ?: return@launch

            val result = when (currentState.step) {
                AuthStep.Email -> {
                    if (currentState.email.isBlank()) {
                        _state.update { it.copy(isLoading = false, error = "Введите email") }
                        return@launch
                    }
                    api.submitUsername(action, currentState.email.trim())
                }

                AuthStep.Password -> {
                    if (currentState.password.isBlank()) {
                        _state.update { it.copy(isLoading = false, error = "Введите пароль") }
                        return@launch
                    }
                    api.submitPassword(action, currentState.password)
                }

                AuthStep.Otp -> {
                    if (currentState.otpCode.isBlank()) {
                        _state.update { it.copy(isLoading = false, error = "Введите код") }
                        return@launch
                    }
                    api.submitOtp(
                        loginAction = action,
                        code = currentState.otpCode.trim(),
                        phoneNumber = currentPhoneNumber.orEmpty(),
                    )
                }
            }

            handleStepResult(result)
        }
    }

    private suspend fun handleStepResult(result: AuthStepResult) {
        logger.info { "handleStepResult: $result" }
        when (result) {
            is AuthStepResult.NextStep -> {
                currentLoginAction = result.loginAction
                if (result.phoneNumber != null) {
                    currentPhoneNumber = result.phoneNumber
                }
                val newStep = mapActivePage(result.activePage)
                val currentStep = _state.value.step
                // If step didn't change and no error from server — show implicit error
                val error = result.errorMessage ?: inferErrorForSameStep(currentStep, newStep)
                _state.update {
                    it.copy(
                        isLoading = false,
                        step = newStep,
                        phoneNumber = result.phoneNumber ?: it.phoneNumber,
                        error = error,
                        otpCode = "",
                    )
                }
            }

            is AuthStepResult.Redirect -> {
                handleRedirect(result.callbackUrl)
            }

            is AuthStepResult.Error -> {
                _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private suspend fun handleRedirect(callbackUrl: String) {
        val api = authApi ?: return
        val bffCookie = api.exchangeCallback(callbackUrl)
        if (bffCookie != null) {
            authRepository.saveCookie(bffCookie)
            val isValid = authRepository.validateCookie()
            if (isValid) {
                logger.info { "Native auth succeeded" }
                api.close()
                authApi = null
                onLoginSuccess()
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Cookie невалиден. Попробуйте через браузер.",
                    )
                }
            }
        } else {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Не удалось получить сессию. Попробуйте через браузер.",
                )
            }
        }
    }

    private fun handleBack() {
        val currentState = _state.value
        when (currentState.step) {
            AuthStep.Email -> { // nothing to go back to
            }

            AuthStep.Password -> {
                // Restart the auth flow for email step
                authApi?.close()
                _state.update {
                    LoginComponent.State(
                        step = AuthStep.Email,
                        email = it.email,
                    )
                }
                startAuthFlow()
            }

            AuthStep.Otp -> {
                // Restart the auth flow for email step
                authApi?.close()
                _state.update {
                    LoginComponent.State(
                        step = AuthStep.Email,
                        email = it.email,
                    )
                }
                startAuthFlow()
            }
        }
    }

    private fun mapActivePage(activePage: String): AuthStep =
        when (activePage) {
            "emailLogin" -> AuthStep.Email
            "password" -> AuthStep.Password
            "otp" -> AuthStep.Otp
            else -> {
                logger.warn { "Unknown Keycloak page: $activePage" }
                AuthStep.Email
            }
        }

    private fun inferErrorForSameStep(
        current: AuthStep,
        next: AuthStep,
    ): String? {
        if (current != next) {
            return null
        }

        return when (current) {
            AuthStep.Email -> "Неверный email"
            AuthStep.Password -> "Неверный логин или пароль"
            AuthStep.Otp -> "Неверный код"
        }
    }
}
