package io.github.kroune.cumobile.di

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.data.local.AndroidFileStorage
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.createDataStore
import io.github.kroune.cumobile.data.local.dataStorePath
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.root.DefaultRootComponent
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * Initializes Koin with the Android platform module.
 * Called from [io.github.kroune.cumobile.AndroidApplication].
 */
fun initKoinAndroid(context: Context) {
    val platformModule = module {
        single { createDataStore { dataStorePath(context) } }
        single<FileStorage> { AndroidFileStorage(context) }
    }
    initKoin(platformModule)
}

/**
 * Creates a [DefaultRootComponent] with dependencies resolved from Koin.
 * Called from [io.github.kroune.cumobile.MainActivity].
 */
fun createRootComponent(componentContext: ComponentContext): DefaultRootComponent {
    val koin = KoinPlatform.getKoin()
    return DefaultRootComponent(
        componentContext = componentContext,
        authRepository = koin.get<AuthRepository>(),
        taskRepository = koin.get<TaskRepository>(),
        courseRepository = koin.get<CourseRepository>(),
        profileRepository = koin.get<ProfileRepository>(),
        performanceRepository = koin.get<PerformanceRepository>(),
        contentRepository = koin.get<ContentRepository>(),
        notificationRepository = koin.get<NotificationRepository>(),
        fileRepository = koin.get<FileRepository>(),
    )
}
