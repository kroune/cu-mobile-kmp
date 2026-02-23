package io.github.kroune.cumobile.presentation.auth.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific WebView composable for authentication.
 * Loads the given [url] and captures the `bff.cookie` session cookie
 * when the user successfully authenticates.
 *
 * @param url The authentication URL to load
 * @param onCookieCaptured Callback invoked with the captured cookie value
 * @param modifier Compose modifier
 */
@Composable
expect fun PlatformWebView(
    url: String,
    onCookieCaptured: (String) -> Unit,
    modifier: Modifier = Modifier,
)
