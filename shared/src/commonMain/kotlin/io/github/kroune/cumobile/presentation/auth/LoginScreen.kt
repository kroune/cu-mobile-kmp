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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.AppColors

private const val APP_VERSION = "1.0.1"

/**
 * Login screen matching the Flutter reference design.
 *
 * Shows app logo, auth instructions, login button, and app version.
 */
@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
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
            onClick = { component.onIntent(LoginComponent.Intent.LoginClicked) },
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
        color = AppColors.Accent,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Авторизация",
        color = AppColors.TextPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun AuthDescription() {
    Text(
        text = "Авторизуйтесь через браузер,\nмы сохраним сессию автоматически.",
        color = AppColors.TextSecondary,
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
                AppColors.Surface,
                RoundedCornerShape(12.dp),
            ).padding(16.dp),
    ) {
        Text(
            text = "Как войти",
            color = AppColors.TextPrimary,
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
        color = AppColors.TextSecondary,
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
            color = AppColors.Accent,
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
                    color = AppColors.Accent,
                    shape = RoundedCornerShape(24.dp),
                ),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppColors.Accent,
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
            color = AppColors.Error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VersionLabel() {
    Text(
        text = "Версия $APP_VERSION",
        color = AppColors.TextSecondary,
        fontSize = 12.sp,
    )
}
