package io.github.kroune.cumobile.presentation.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.kroune.cumobile.presentation.auth.LoginScreen
import io.github.kroune.cumobile.presentation.auth.webview.WebViewLoginScreen
import io.github.kroune.cumobile.presentation.main.MainScreen
import io.github.kroune.cumobile.presentation.splash.SplashScreen

/**
 * Root composable that renders the current child of [RootComponent].
 * Uses Decompose's [Children] with fade animation for transitions.
 */
@Composable
fun RootScreen(component: RootComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade()),
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.SplashChild -> SplashScreen()
            is RootComponent.Child.LoginChild -> LoginScreen(instance.component)
            is RootComponent.Child.WebViewLoginChild -> WebViewLoginScreen(instance.component)
            is RootComponent.Child.MainChild -> MainScreen(instance.component)
        }
    }
}
