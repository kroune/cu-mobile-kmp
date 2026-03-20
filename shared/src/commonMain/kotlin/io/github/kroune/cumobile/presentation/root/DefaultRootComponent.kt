package io.github.kroune.cumobile.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.presentation.auth.DefaultLoginComponent
import io.github.kroune.cumobile.presentation.auth.webview.DefaultWebViewLoginComponent
import io.github.kroune.cumobile.presentation.main.DefaultMainComponent
import io.github.kroune.cumobile.presentation.main.MainDependencies
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [RootComponent].
 *
 * Uses Decompose [ChildStack] to manage auth routing between login and main flows.
 * On startup, checks for a saved auth cookie and navigates to [Config.Main] if valid.
 * Native login flow: [Config.Login] → [Config.Main].
 * WebView fallback: [Config.Login] → [Config.WebViewLogin] → [Config.Main].
 */
@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val mainDependencies: MainDependencies,
) : RootComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Splash,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    init {
        checkSavedAuth()
    }

    private fun checkSavedAuth() {
        scope.launch {
            if (authRepository.hasCookie()) {
                logger.info { "checkSavedAuth: cookie found locally, navigating to Main" }
                navigateToMain()
                validateCookieInBackground()
            } else {
                logger.info { "checkSavedAuth: no cookie, navigating to Login" }
                navigation.replaceAll(Config.Login)
            }
        }
    }

    private fun validateCookieInBackground() {
        scope.launch {
            if (!authRepository.validateCookie()) {
                logger.warn { "validateCookieInBackground: cookie invalid, redirecting to Login" }
                navigation.replaceAll(Config.Login)
            }
        }
    }

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): RootComponent.Child =
        when (config) {
            is Config.Splash -> RootComponent.Child.SplashChild
            is Config.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext = childContext,
                    authRepository = authRepository,
                    onLoginSuccess = ::navigateToMain,
                    onNavigateToWebView = ::navigateToWebView,
                ),
            )
            is Config.WebViewLogin -> RootComponent.Child.WebViewLoginChild(
                DefaultWebViewLoginComponent(
                    componentContext = childContext,
                    authRepository = authRepository,
                    onLoginSuccess = ::navigateToMain,
                    onBack = ::navigateBackFromWebView,
                ),
            )
            is Config.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext = childContext,
                    mainDependencies = mainDependencies,
                    onLogout = ::handleLogout,
                ),
            )
        }

    private fun navigateToWebView() {
        navigation.push(Config.WebViewLogin)
    }

    private fun navigateBackFromWebView() {
        navigation.pop()
    }

    private fun navigateToMain() {
        navigation.replaceAll(Config.Main)
    }

    private fun handleLogout() {
        scope.launch {
            authRepository.clearCookie()
            navigation.replaceAll(Config.Login)
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Splash : Config

        @Serializable
        data object Login : Config

        @Serializable
        data object WebViewLogin : Config

        @Serializable
        data object Main : Config
    }
}
