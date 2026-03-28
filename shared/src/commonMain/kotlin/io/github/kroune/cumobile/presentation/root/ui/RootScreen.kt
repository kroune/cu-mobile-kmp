package io.github.kroune.cumobile.presentation.root.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.FaultyDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.kroune.cumobile.presentation.auth.ui.LoginScreen
import io.github.kroune.cumobile.presentation.auth.webview.ui.WebViewLoginScreen
import io.github.kroune.cumobile.presentation.main.ui.MainScreen
import io.github.kroune.cumobile.presentation.root.RootComponent
import io.github.kroune.cumobile.presentation.splash.ui.SplashScreen

/**
 * Root composable that renders the current child of [RootComponent].
 * Uses Decompose's [Children] with fade animation for transitions between non-splash screens,
 * and disables animations when a transition involves the splash screen.
 */
@OptIn(FaultyDecomposeApi::class)
@Composable
fun RootScreen(component: RootComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation { child, otherChild, _ ->
            val splashInvolved = child.instance is RootComponent.Child.SplashChild ||
                otherChild.instance is RootComponent.Child.SplashChild
            if (splashInvolved) null else fade()
        },
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.SplashChild -> SplashScreen()
            is RootComponent.Child.LoginChild -> LoginScreen(instance.component)
            is RootComponent.Child.WebViewLoginChild -> WebViewLoginScreen(instance.component)
            is RootComponent.Child.MainChild -> MainScreen(instance.component)
        }
    }
}
