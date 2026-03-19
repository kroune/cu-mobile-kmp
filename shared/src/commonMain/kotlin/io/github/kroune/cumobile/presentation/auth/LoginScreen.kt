@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme

private const val AppVersion = "1.0.1"

/**
 * Login screen matching the Flutter reference design.
 *
 * Shows app logo, auth instructions, login button, and app version.
 */
@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.subscribeAsState()

    LoginScreenContent(
        state = state,
        onLoginClick = { component.onIntent(LoginComponent.Intent.LoginClicked) },
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginComponent.State,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        LogoSection()
        Spacer(modifier = Modifier.height(32.dp))
        AuthDescription()
        Spacer(modifier = Modifier.height(24.dp))
        HowToLoginSection()
        Spacer(modifier = Modifier.height(32.dp))
        LoginButton(
            isLoading = state.isLoading,
            onClick = onLoginClick,
        )
        ErrorMessage(error = state.error)

        Spacer(modifier = Modifier.weight(1f))

        VersionLabel()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LogoSection() {
    // Text-based logo (CU doesn't provide a shared SVG we can embed)
    Text(
        text = "ЦУ",
        color = AppTheme.colors.accent,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Авторизация",
        color = AppTheme.colors.textPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun AuthDescription() {
    Text(
        text = "Авторизуйтесь через браузер,\nмы сохраним сессию автоматически.",
        color = AppTheme.colors.textSecondary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun HowToLoginSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppTheme.colors.surface,
                RoundedCornerShape(12.dp),
            ).padding(16.dp),
    ) {
        Text(
            text = "Как войти",
            color = AppTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LoginStep(number = "1", text = "Нажмите «Войти через браузер»")
        LoginStep(number = "2", text = "Введите логин и пароль от ЦУ")
        LoginStep(number = "3", text = "Сессия сохранится автоматически")
    }
}

@Composable
private fun LoginStep(
    number: String,
    text: String,
) {
    Text(
        text = "$number. $text",
        color = AppTheme.colors.textSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    if (isLoading) {
        CircularProgressIndicator(
            color = AppTheme.colors.accent,
            modifier = Modifier.size(40.dp),
        )
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.accent,
                    shape = RoundedCornerShape(24.dp),
                ),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppTheme.colors.accent,
            ),
        ) {
            Text(
                text = "Войти через браузер",
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun ErrorMessage(error: String?) {
    if (error != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = AppTheme.colors.error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VersionLabel() {
    Text(
        text = "Версия $AppVersion",
        color = AppTheme.colors.textSecondary,
        fontSize = 12.sp,
    )
}

@Preview
@Composable
private fun PreviewLoginScreenDark() {
    CuMobileTheme(darkTheme = true) {
        LoginScreenContent(state = LoginComponent.State(), onLoginClick = {})
    }
}

@Preview
@Composable
private fun PreviewLoginScreenLight() {
    CuMobileTheme(darkTheme = false) {
        LoginScreenContent(state = LoginComponent.State(), onLoginClick = {})
    }
}
