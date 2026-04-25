package io.github.kroune.cumobile.presentation.auth.sms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.github.oshai.kotlinlogging.KotlinLogging
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIPasteboard
import platform.UIKit.UIPasteboardChangedNotification

private val logger = KotlinLogging.logger {}

/**
 * iOS has no public API to read SMS messages. The system does surface OTPs via
 * the QuickType keyboard bar when a text field is marked with
 * `UITextContentType.oneTimeCode`, but Compose Multiplatform does not currently
 * expose that hint, so we fall back to the pasteboard:
 *
 *  - When the user copies a code (e.g. via "Copy Code" from the SMS banner, or
 *    manually from Messages) we receive `UIPasteboardChangedNotification`.
 *  - When the user returns to this app from Messages we re-check the pasteboard
 *    to catch the case where the copy happened while backgrounded.
 *
 * Each detected code is delivered to [onCode] at most once per pasteboard write.
 */
@Composable
actual fun SmsCodeObserver(
    enabled: Boolean,
    onCode: (String) -> Unit,
) {
    val latestOnCode by rememberUpdatedState(onCode)

    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        val center = NSNotificationCenter.defaultCenter
        val pasteboard = UIPasteboard.generalPasteboard
        var lastHandledChangeCount = pasteboard.changeCount

        fun check(reason: String) {
            val current = pasteboard.changeCount
            if (current == lastHandledChangeCount) return
            lastHandledChangeCount = current
            val value = pasteboard.string ?: return
            val code = extractOtpCode(value) ?: return
            logger.info { "iOS SmsCodeObserver: code detected via $reason, length=${code.length}" }
            latestOnCode(code)
        }

        val pasteboardObserver = center.addObserverForName(
            name = UIPasteboardChangedNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> check("pasteboard change") }

        val activeObserver = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> check("app active") }

        onDispose {
            center.removeObserver(pasteboardObserver)
            center.removeObserver(activeObserver)
        }
    }
}
