@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.splash.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme

@Preview
@Composable
private fun PreviewSplashDark() {
    CuMobileTheme(darkTheme = true) {
        SplashScreen()
    }
}

@Preview
@Composable
private fun PreviewSplashLight() {
    CuMobileTheme(darkTheme = false) {
        SplashScreen()
    }
}
