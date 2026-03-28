package io.github.kroune.cumobile.presentation.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.auth.LoginComponent
import io.github.kroune.cumobile.presentation.auth.LoginComponent.AuthStep
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme

@Preview
@Composable
private fun PreviewEmailDark() {
    CuMobileTheme(darkTheme = true) {
        LoginScreenContent(state = LoginComponent.State(email = "test@edu.cu.ru"), onIntent = {})
    }
}

@Preview
@Composable
private fun PreviewPasswordDark() {
    CuMobileTheme(darkTheme = true) {
        LoginScreenContent(
            state = LoginComponent.State(step = AuthStep.Password, email = "test@edu.cu.ru"),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewOtpDark() {
    CuMobileTheme(darkTheme = true) {
        LoginScreenContent(
            state = LoginComponent.State(step = AuthStep.Otp, phoneNumber = "+7 *** *** 76 24"),
            onIntent = {},
        )
    }
}
