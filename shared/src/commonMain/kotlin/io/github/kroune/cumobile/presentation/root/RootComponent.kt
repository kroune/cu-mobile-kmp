package io.github.kroune.cumobile.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.auth.LoginComponent
import io.github.kroune.cumobile.presentation.auth.webview.WebViewLoginComponent
import io.github.kroune.cumobile.presentation.main.MainComponent

/**
 * Root navigation component that manages the top-level auth routing.
 * Switches between login flow screens and the main app based on authentication state.
 */
interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class LoginChild(
            val component: LoginComponent,
        ) : Child()

        class WebViewLoginChild(
            val component: WebViewLoginComponent,
        ) : Child()

        class MainChild(
            val component: MainComponent,
        ) : Child()
    }
}
