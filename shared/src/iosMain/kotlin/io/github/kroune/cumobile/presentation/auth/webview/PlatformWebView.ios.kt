package io.github.kroune.cumobile.presentation.auth.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private const val TARGET_COOKIE_NAME = "bff.cookie"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    onCookieCaptured: (String) -> Unit,
    modifier: Modifier,
) {
    val delegate = remember {
        WebViewNavigationDelegate(onCookieCaptured)
    }

    UIKitView(
        factory = {
            val configuration = WKWebViewConfiguration()
            val webView = WKWebView(
                frame = CGRectMake(x = 0.0, y = 0.0, width = 0.0, height = 0.0),
                configuration = configuration,
            )
            webView.navigationDelegate = delegate
            val nsUrl = NSURL.URLWithString(url)!!
            webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            webView
        },
        modifier = modifier,
    )
}

/**
 * WKWebView navigation delegate that intercepts the bff.cookie
 * after successful authentication.
 */
private class WebViewNavigationDelegate(
    private val onCookieCaptured: (String) -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
    private var cookieFound = false

    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        if (!cookieFound) {
            checkCookies(webView)
        }
    }

    private fun checkCookies(webView: WKWebView) {
        webView.configuration.websiteDataStore.httpCookieStore.getAllCookies { cookies ->
            if (cookies == null) return@getAllCookies
            for (item in cookies) {
                val httpCookie = item as? NSHTTPCookie ?: continue
                if (httpCookie.name == TARGET_COOKIE_NAME && httpCookie.value.isNotEmpty()) {
                    cookieFound = true
                    onCookieCaptured(httpCookie.value)
                    return@getAllCookies
                }
            }
        }
    }
}
