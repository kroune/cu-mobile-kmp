package io.github.kroune.cumobile.di

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.data.network.createHttpClient
import io.github.kroune.cumobile.data.repository.AuthRepositoryImpl
import io.github.kroune.cumobile.data.repository.ContentRepositoryImpl
import io.github.kroune.cumobile.data.repository.CourseRepositoryImpl
import io.github.kroune.cumobile.data.repository.FileRepositoryImpl
import io.github.kroune.cumobile.data.repository.NotificationRepositoryImpl
import io.github.kroune.cumobile.data.repository.PerformanceRepositoryImpl
import io.github.kroune.cumobile.data.repository.ProfileRepositoryImpl
import io.github.kroune.cumobile.data.repository.TaskRepositoryImpl
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Core Koin module providing shared dependencies.
 * Platform-specific modules (e.g., DataStore) are provided via [platformModule].
 */
val networkModule = module {
    single { createHttpClient() }
    single { ApiService(get()) }
}

val dataModule = module {
    single { AuthLocalDataSource(get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<CourseRepository> { CourseRepositoryImpl(get(), get()) }
    single<ContentRepository> { ContentRepositoryImpl(get(), get()) }
    single<FileRepository> { FileRepositoryImpl(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get()) }
    single<PerformanceRepository> { PerformanceRepositoryImpl(get(), get()) }
}

/**
 * Initializes Koin with common modules and an optional platform-specific module.
 * @param platformModule platform-specific module providing DataStore, etc.
 */
fun initKoin(platformModule: Module = module { }) {
    startKoin {
        modules(
            platformModule,
            networkModule,
            dataModule,
            repositoryModule,
        )
    }
}
