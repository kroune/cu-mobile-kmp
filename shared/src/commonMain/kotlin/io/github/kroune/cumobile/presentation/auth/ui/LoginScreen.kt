package io.github.kroune.cumobile.presentation.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.auth.LoginComponent
import io.github.kroune.cumobile.presentation.auth.LoginComponent.AuthStep
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

private const val AppVersion = "1.0.1"

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.subscribeAsState()
    LoginScreenContent(state = state, onIntent = component::onIntent)
}

@Composable
internal fun LoginScreenContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
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

        // Logo
        Text(
            text = "ЦУ",
            color = AppTheme.colors.accent,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(targetState = state.step, label = "auth_step") { step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (step) {
                    AuthStep.Email -> EmailStepContent(state, onIntent)
                    AuthStep.Password -> PasswordStepContent(state, onIntent)
                    AuthStep.Otp -> OtpStepContent(state, onIntent)
                }
            }
        }

        // Error
        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error,
                color = AppTheme.colors.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fallback to WebView
        TextButton(onClick = { onIntent(LoginComponent.Intent.FallbackToWebView) }) {
            Text(
                text = "Войти через браузер",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Version
        Text(
            text = "Версия $AppVersion",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EmailStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    StepHeader(title = "Авторизация", subtitle = "Введите email от аккаунта ЦУ")
    Spacer(modifier = Modifier.height(24.dp))
    AuthTextField(
        value = state.email,
        onValueChange = { onIntent(LoginComponent.Intent.UpdateEmail(it)) },
        label = "Email",
        keyboardType = KeyboardType.Email,
        onDone = { onIntent(LoginComponent.Intent.Submit) },
    )
    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(text = "Далее", isLoading = state.isLoading) {
        onIntent(LoginComponent.Intent.Submit)
    }
}

@Composable
private fun PasswordStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    BackRow(onIntent)
    StepHeader(title = "Введите пароль", subtitle = state.email)
    Spacer(modifier = Modifier.height(24.dp))
    AuthTextField(
        value = state.password,
        onValueChange = { onIntent(LoginComponent.Intent.UpdatePassword(it)) },
        label = "Пароль",
        isPassword = true,
        onDone = { onIntent(LoginComponent.Intent.Submit) },
    )
    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(text = "Войти", isLoading = state.isLoading) {
        onIntent(LoginComponent.Intent.Submit)
    }
}

@Composable
private fun OtpStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    BackRow(onIntent)
    StepHeader(
        title = "Код из SMS",
        subtitle = state.phoneNumber?.let { "Код отправлен на $it" }.orEmpty(),
    )
    Spacer(modifier = Modifier.height(24.dp))
    AuthTextField(
        value = state.otpCode,
        onValueChange = { onIntent(LoginComponent.Intent.UpdateOtpCode(it)) },
        label = "Код подтверждения",
        keyboardType = KeyboardType.Number,
        onDone = { onIntent(LoginComponent.Intent.Submit) },
    )
    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(text = "Подтвердить", isLoading = state.isLoading) {
        onIntent(LoginComponent.Intent.Submit)
    }
}

@Composable
private fun StepHeader(
    title: String,
    subtitle: String,
) {
    Text(
        text = title,
        color = AppTheme.colors.textPrimary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = subtitle,
        color = AppTheme.colors.textSecondary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun BackRow(onIntent: (LoginComponent.Intent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        IconButton(onClick = { onIntent(LoginComponent.Intent.Back) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = AppTheme.colors.accent,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.accent,
            unfocusedBorderColor = AppTheme.colors.textSecondary.copy(alpha = 0.4f),
            focusedLabelColor = AppTheme.colors.accent,
            unfocusedLabelColor = AppTheme.colors.textSecondary,
            cursorColor = AppTheme.colors.accent,
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
        ),
    )
}

@Composable
private fun SubmitButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    if (isLoading) {
        CircularProgressIndicator(color = AppTheme.colors.accent, modifier = Modifier.size(40.dp))
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, AppTheme.colors.accent, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.accent),
        ) {
            Text(text = text, fontSize = 16.sp)
        }
    }
}
