package io.github.kroune.cumobile.presentation.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.baseline.BaselineTestTags
import io.github.kroune.cumobile.presentation.auth.LoginComponent
import io.github.kroune.cumobile.presentation.auth.LoginComponent.AuthStep
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val AppVersion = "1.0.1"

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.subscribeAsState()
    LoginScreenContent(
        state = state,
        effects = component.effects,
        onIntent = component::onIntent,
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginComponent.State,
    effects: Flow<LoginComponent.Effect> = emptyFlow(),
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is LoginComponent.Effect.ShowError -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBackBar(
                visible = state.step != AuthStep.Email,
                onBack = { onIntent(LoginComponent.Intent.Back) },
            )
            Spacer(modifier = Modifier.weight(1f))
            LoginLogo()
            StepContainer(state = state, onIntent = onIntent)
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryLoginActions(step = state.step, onIntent = onIntent)
            Spacer(modifier = Modifier.weight(1f))
            VersionFooter()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppTheme.colors.error.copy(alpha = 0.95f),
                contentColor = AppTheme.colors.background,
            )
        }
    }
}

@Composable
private fun LoginLogo() {
    Text(
        text = "ЦУ",
        color = AppTheme.colors.accent,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun StepContainer(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    AnimatedContent(targetState = state.step, label = "auth_step") { step ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (step) {
                AuthStep.Email -> EmailStepContent(state, onIntent)
                AuthStep.Password -> PasswordStepContent(state, onIntent)
                AuthStep.Otp -> OtpStepContent(state, onIntent)
                AuthStep.BffCookie -> BffCookieStepContent(state, onIntent)
            }
        }
    }
}

@Composable
private fun SecondaryLoginActions(
    step: AuthStep,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    TextButton(onClick = { onIntent(LoginComponent.Intent.FallbackToWebView) }) {
        Text(
            text = "Войти через браузер",
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
        )
    }
    if (step != AuthStep.BffCookie) {
        TextButton(
            onClick = { onIntent(LoginComponent.Intent.OpenBffCookieLogin) },
            modifier = Modifier.testTag(BaselineTestTags.LOGIN_SWITCH_BFF),
        ) {
            Text(
                text = "Войти по bff.cookie",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun VersionFooter() {
    Text(
        text = "Версия $AppVersion",
        color = AppTheme.colors.textSecondary,
        fontSize = 12.sp,
    )
    Spacer(modifier = Modifier.height(16.dp))
}

/**
 * Fixed-height top row. Always reserves the same height regardless of [visible]
 * so switching steps doesn't shift the rest of the layout.
 */
@Composable
private fun TopBackBar(
    visible: Boolean,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (visible) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = AppTheme.colors.accent,
                )
            }
        }
    }
}
