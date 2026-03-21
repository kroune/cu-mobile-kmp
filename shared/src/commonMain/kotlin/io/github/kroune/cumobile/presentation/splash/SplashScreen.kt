package io.github.kroune.cumobile.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cumobile.shared.generated.resources.Res
import cumobile.shared.generated.resources.app_logo
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.DarkAppColors
import org.jetbrains.compose.resources.painterResource

private val SplashIconSize = 192.dp

/**
 * Splash screen shown briefly during startup while auth state is resolved.
 * Always uses dark background because the app logo is white.
 */
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAppColors.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(SplashIconSize),
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
