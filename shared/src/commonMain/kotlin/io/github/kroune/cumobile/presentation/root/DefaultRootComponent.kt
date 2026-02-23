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
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.auth.DefaultLoginComponent
import io.github.kroune.cumobile.presentation.auth.webview.DefaultWebViewLoginComponent
import io.github.kroune.cumobile.presentation.main.DefaultMainComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Default implementation of [RootComponent].
 *
 * Uses Decompose [ChildStack] to manage auth routing between login and main flows.
 * On startup, checks for a saved auth cookie and navigates to [Config.Main] if valid.
 * Otherwise starts at [Config.Login] → [Config.WebViewLogin] → [Config.Main].
 */
@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val courseRepository: CourseRepository,
    private val profileRepository: ProfileRepository,
    private val performanceRepository: PerformanceRepository,
    private val contentRepository: ContentRepository,
    private val notificationRepository: NotificationRepository,
    private val fileRepository: FileRepository,
) : RootComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Login,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onDestroy() {
                    scope.cancel()
                }
            },
        )
        checkSavedAuth()
    }

    /**
     * Checks if a previously saved cookie is still valid.
     * If valid, navigates directly to the main screen.
     */
    private fun checkSavedAuth() {
        scope.launch {
            val isValid = authRepository.validateCookie()
            if (isValid) {
                navigateToMain()
            }
        }
    }

    private fun createChild(
        config: Config,
        childContext: ComponentContext,
    ): RootComponent.Child =
        when (config) {
            is Config.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext = childContext,
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
                    taskRepository = taskRepository,
                    courseRepository = courseRepository,
                    profileRepository = profileRepository,
                    performanceRepository = performanceRepository,
                    contentRepository = contentRepository,
                    notificationRepository = notificationRepository,
                    fileRepository = fileRepository,
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
        data object Login : Config

        @Serializable
        data object WebViewLogin : Config

        @Serializable
        data object Main : Config
    }
}
