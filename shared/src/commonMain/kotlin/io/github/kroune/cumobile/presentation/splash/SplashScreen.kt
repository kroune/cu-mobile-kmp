@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u0426\u0423",
            color = AppTheme.colors.accent,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

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
