package io.github.kroune.cumobile

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.main.MainDependencies
import io.github.kroune.cumobile.presentation.root.DefaultRootComponent
import io.github.kroune.cumobile.presentation.root.RootComponent
import org.koin.mp.KoinPlatform

fun rootViewController(root: RootComponent) = ComposeUIViewController { App(root) }

/**
 * Creates a [DefaultRootComponent] with dependencies resolved from Koin.
 * Called from iOS Swift entry point (iOSApp.swift).
 */
fun createRootComponent(componentContext: ComponentContext): DefaultRootComponent {
    val koin = KoinPlatform.getKoin()
    val mainDependencies = MainDependencies(
        taskRepository = koin.get<TaskRepository>(),
        courseRepository = koin.get<CourseRepository>(),
        profileRepository = koin.get<ProfileRepository>(),
        performanceRepository = koin.get<PerformanceRepository>(),
        contentRepository = koin.get<ContentRepository>(),
        notificationRepository = koin.get<NotificationRepository>(),
        fileRepository = koin.get<FileRepository>(),
    )
    return DefaultRootComponent(
        componentContext = componentContext,
        authRepository = koin.get<AuthRepository>(),
        mainDependencies = mainDependencies,
    )
}
