package io.github.kroune.cumobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.root.RootComponent
import io.github.kroune.cumobile.presentation.root.RootScreen

private val DarkColors = darkColorScheme(
    background = AppColors.Background,
    surface = AppColors.Surface,
    primary = AppColors.Accent,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    error = AppColors.Error,
)

private val LightColors = lightColorScheme(
    primary = AppColors.Accent,
    error = AppColors.Error,
)

/**
 * App entry point composable.
 * Renders the [RootScreen] which handles auth routing via Decompose.
 */
@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors) {
        RootScreen(rootComponent)
    }
}
