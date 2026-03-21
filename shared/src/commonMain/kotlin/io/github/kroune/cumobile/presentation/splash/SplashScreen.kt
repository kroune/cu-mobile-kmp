package io.github.kroune.cumobile.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cumobile.shared.generated.resources.Res
import cumobile.shared.generated.resources.app_logo
import org.jetbrains.compose.resources.painterResource

private val SplashBackground = Color(0xFF121212)
private val SplashIconSize = 192.dp

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
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
    SplashScreen()
}
