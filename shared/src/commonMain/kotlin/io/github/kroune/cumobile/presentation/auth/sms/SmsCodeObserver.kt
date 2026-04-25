package io.github.kroune.cumobile.presentation.auth.sms

import androidx.compose.runtime.Composable

/**
 * Composable that listens for incoming SMS messages and extracts a one-time code.
 *
 * Android: uses Google Play Services' SMS User Consent API — the system shows a
 * one-tap consent prompt, then delivers the SMS body to the app. No runtime permissions.
 * iOS: no-op. iOS surfaces OTPs via the keyboard suggestion strip based on the input's
 * content type; Compose Multiplatform does not currently expose that hint, so nothing
 * to do here.
 *
 * The observer is active only while composed and [enabled] is true. [onCode] fires
 * at most once per delivered SMS.
 */
@Composable
expect fun SmsCodeObserver(
    enabled: Boolean = true,
    onCode: (String) -> Unit,
)

/**
 * Extracts the first run of [minLen]..[maxLen] digits from [body].
 * Shared between platform implementations and WebView code injection.
 */
internal fun extractOtpCode(
    body: String,
    minLen: Int = OtpMinLen,
    maxLen: Int = OtpMaxLen,
): String? {
    val regex = Regex("\\d{$minLen,$maxLen}")
    return regex.find(body)?.value
}

internal const val OtpMinLen = 4
internal const val OtpMaxLen = 8
