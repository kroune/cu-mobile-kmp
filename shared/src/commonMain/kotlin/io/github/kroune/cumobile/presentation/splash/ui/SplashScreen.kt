package io.github.kroune.cumobile.presentation.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import cumobile.shared.generated.resources.Res
import cumobile.shared.generated.resources.app_logo
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import org.jetbrains.compose.resources.painterResource

private val SplashIconSize = 192.dp

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(SplashIconSize),
            colorFilter = ColorFilter.tint(AppTheme.colors.textPrimary),
        )
    }
}
