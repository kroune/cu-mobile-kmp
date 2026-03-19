package io.github.kroune.cumobile.presentation.auth.webview

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.kroune.cumobile.data.network.BaseDomain
import io.github.kroune.cumobile.data.network.TargetCookieName
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    onCookieCaptured: (String) -> Unit,
    modifier: Modifier,
) {
    val captureState = remember { CookieCaptureState() }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                val webView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webView, true)
                }

                webViewClient = createAuthWebViewClient(onCookieCaptured, captureState)
                loadUrl(url)
            }
        },
        modifier = modifier,
    )
}

private fun createAuthWebViewClient(
    onCookieCaptured: (String) -> Unit,
    captureState: CookieCaptureState,
): WebViewClient =
    object : WebViewClient() {
        override fun onPageFinished(
            view: WebView?,
            url: String?,
        ) {
            super.onPageFinished(view, url)
            tryCaptureCookie(onCookieCaptured, captureState)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            tryCaptureCookie(onCookieCaptured, captureState)
            return false
        }
    }

private fun tryCaptureCookie(
    onCookieCaptured: (String) -> Unit,
    captureState: CookieCaptureState,
) {
    if (captureState.isCaptured) return
    val allCookies = CookieManager.getInstance().getCookie(BaseDomain)
    logger.debug { "All cookies for $BaseDomain: $allCookies" }
    val cookie = extractBffCookie()
    if (cookie == null) {
        logger.debug { "bff.cookie not found yet" }
        return
    }
    logger.info { "bff.cookie captured, length=${cookie.length}, prefix=${cookie.take(20)}..." }
    captureState.isCaptured = true
    onCookieCaptured(cookie)
}

private fun extractBffCookie(): String? {
    val cookieManager = CookieManager.getInstance()
    val cookieString = cookieManager
        .getCookie(BaseDomain)
        ?: return null
    return cookieString
        .split(";")
        .asSequence()
        .map { it.trim().split("=", limit = 2) }
        .firstOrNull { it.size == 2 && it[0].trim() == TargetCookieName }
        ?.get(1)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

/**
 * Mutable flag to prevent capturing the cookie multiple times.
 * Used as a stable reference inside AndroidView callbacks.
 */
private class CookieCaptureState {
    var isCaptured: Boolean = false
}
