package io.github.kroune.cumobile.presentation.auth.webview.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import io.github.kroune.cumobile.data.network.BaseDomain
import io.github.kroune.cumobile.data.network.TargetCookieName
import io.github.kroune.cumobile.presentation.auth.sms.SmsCodeObserver
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

private const val CookieLogPrefixLen = 20

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    onCookieCaptured: (String) -> Unit,
    modifier: Modifier,
) {
    val captureState = remember { CookieCaptureState() }
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                settings.saveFormData = true

                // Allow system autofill service (Google / password managers) to fill form fields.
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES

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

    SmsCodeObserver { code ->
        // Dumping the code to the clipboard is simpler and more robust than
        // DOM-poking: the keyboard's clipboard suggestion picks it up across
        // any layout the auth page happens to use.
        copyCodeToClipboard(context, code)
    }
}

private fun copyCodeToClipboard(
    context: Context,
    code: String,
) {
    val clipboard = context.getSystemService<ClipboardManager>()
    if (clipboard == null) {
        logger.warn { "ClipboardManager unavailable; cannot autofill OTP in WebView" }
        return
    }
    clipboard.setPrimaryClip(ClipData.newPlainText("otp", code))
    logger.info { "Copied OTP code to clipboard for WebView autofill, length=${code.length}" }
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
    logger.info { "bff.cookie captured, length=${cookie.length}, prefix=${cookie.take(CookieLogPrefixLen)}..." }
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
