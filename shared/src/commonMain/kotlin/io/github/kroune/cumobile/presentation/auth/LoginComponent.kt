package io.github.kroune.cumobile.presentation.auth

import com.arkivanov.decompose.value.Value

/**
 * Component managing the native login flow.
 * Handles multi-step Keycloak authentication: email → password → SMS OTP.
 * Falls back to WebView-based auth if needed.
 */
interface LoginComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    /**
     * Current step of the auth flow.
     */
    enum class AuthStep {
        /** Initial screen with email input. */
        Email,

        /** Password input after email was accepted. */
        Password,

        /** SMS OTP code input after password was accepted. */
        Otp,
    }

    data class State(
        val step: AuthStep = AuthStep.Email,
        val isLoading: Boolean = false,
        val error: String? = null,
        val email: String = "",
        val password: String = "",
        val otpCode: String = "",
        /** Masked phone number shown on OTP step (e.g. "+7 *** *** 76 24"). */
        val phoneNumber: String? = null,
    )

    sealed interface Intent {
        data class UpdateEmail(
            val value: String,
        ) : Intent

        data class UpdatePassword(
            val value: String,
        ) : Intent

        data class UpdateOtpCode(
            val value: String,
        ) : Intent

        /** Submit the current step (email, password, or OTP). */
        data object Submit : Intent

        /** Go back to previous step. */
        data object Back : Intent

        /** Fall back to WebView-based authentication. */
        data object FallbackToWebView : Intent
    }
}
