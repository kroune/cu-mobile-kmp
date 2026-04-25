package io.github.kroune.cumobile.presentation.auth.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Composable
actual fun SmsCodeObserver(
    enabled: Boolean,
    onCode: (String) -> Unit,
) {
    val context = LocalContext.current
    val latestOnCode by rememberUpdatedState(onCode)

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConsentResult(result.resultCode, result.data, latestOnCode)
    }

    val receiver = remember {
        SmsConsentReceiver { consentIntent ->
            runCatching {
                consentLauncher.launch(consentIntent)
            }.onFailure { e -> logger.error(e) { "Failed to launch SMS consent intent" } }
        }
    }

    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose {}
        }
        logger.info { "Starting SMS User Consent listener" }
        SmsRetriever
            .getClient(context)
            .startSmsUserConsent(null)
            .addOnFailureListener { e ->
                logger.error(e) { "startSmsUserConsent failed — Play Services may be unavailable" }
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            SmsRetriever.SEND_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED,
        )
        onDispose {
            runCatching {
                context.unregisterReceiver(receiver)
            }.onFailure { e ->
                logger.warn(e) { "Failed to unregister SMS receiver" }
            }
        }
    }
}

/**
 * BroadcastReceiver for the SMS User Consent API. Lives outside the composable so
 * the UI layer doesn't declare platform components inline — construction only needs
 * a callback to forward the system-provided consent Intent.
 */
private class SmsConsentReceiver(
    private val onConsentIntent: (Intent) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
        val extras = intent.extras ?: return
        val status = BundleCompat.getParcelable(
            extras,
            SmsRetriever.EXTRA_STATUS,
            Status::class.java,
        ) ?: return
        when (status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                val consentIntent = BundleCompat.getParcelable(
                    extras,
                    SmsRetriever.EXTRA_CONSENT_INTENT,
                    Intent::class.java,
                )
                if (consentIntent != null) onConsentIntent(consentIntent)
            }

            CommonStatusCodes.TIMEOUT ->
                logger.info { "SMS User Consent API timed out" }

            else -> logger.warn {
                "SMS User Consent API unexpected status: ${status.statusCode}"
            }
        }
    }
}

private fun handleConsentResult(
    resultCode: Int,
    data: Intent?,
    onCode: (String) -> Unit,
) {
    if (resultCode != Activity.RESULT_OK) {
        logger.info { "SMS consent declined or cancelled: $resultCode" }
        return
    }
    val message = data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
    if (message == null) {
        logger.warn { "SMS consent result missing EXTRA_SMS_MESSAGE" }
        return
    }
    val code = extractOtpCode(message)
    if (code != null) {
        logger.info { "Extracted OTP code from SMS, length=${code.length}" }
        onCode(code)
    } else {
        logger.warn { "No OTP code found in SMS message" }
    }
}
