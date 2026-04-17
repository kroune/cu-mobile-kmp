package io.github.kroune.cumobile.presentation.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.kroune.cumobile.presentation.auth.LoginComponent
import io.github.kroune.cumobile.presentation.auth.sms.SmsCodeObserver
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

@Composable
internal fun EmailStepContent(
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
internal fun PasswordStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
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
internal fun OtpStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
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
    SmsCodeObserver(enabled = !state.isLoading) { code ->
        onIntent(LoginComponent.Intent.UpdateOtpCode(code))
        onIntent(LoginComponent.Intent.Submit)
    }
    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(text = "Подтвердить", isLoading = state.isLoading) {
        onIntent(LoginComponent.Intent.Submit)
    }
}

@Composable
internal fun BffCookieStepContent(
    state: LoginComponent.State,
    onIntent: (LoginComponent.Intent) -> Unit,
) {
    StepHeader(
        title = "bff.cookie",
        subtitle = "Вставьте значение cookie для прямого входа",
    )
    Spacer(modifier = Modifier.height(24.dp))
    AuthTextField(
        value = state.bffCookie,
        onValueChange = { onIntent(LoginComponent.Intent.UpdateBffCookie(it)) },
        label = "bff.cookie",
        keyboardType = KeyboardType.Password,
        onDone = { onIntent(LoginComponent.Intent.Submit) },
    )
    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(text = "Войти", isLoading = state.isLoading) {
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

/**
 * Stays in place during loading: the label and spinner are swapped via
 * [AnimatedContent] so the button keeps its size and the surrounding layout
 * doesn't shift.
 */
@Composable
private fun SubmitButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = { if (!isLoading) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, AppTheme.colors.accent, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.accent),
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(targetState = isLoading, label = "submit_state") { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(text = text, fontSize = 16.sp)
                }
            }
        }
    }
}
