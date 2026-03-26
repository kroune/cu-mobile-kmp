package io.github.kroune.cumobile.di

import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CalendarLocalDataSource
import io.github.kroune.cumobile.data.local.CourseLocalDataSource
import io.github.kroune.cumobile.data.local.FileOpener
import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.PdfGenerator
import io.github.kroune.cumobile.data.local.db.AppDatabase
import io.github.kroune.cumobile.data.network.ContentApiService
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.data.network.IcalApiService
import io.github.kroune.cumobile.data.network.IcalParser
import io.github.kroune.cumobile.data.network.NotificationApiService
import io.github.kroune.cumobile.data.network.PerformanceApiService
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.data.network.TaskApiService
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.data.network.UpdateChecker
import io.github.kroune.cumobile.data.network.createHttpClient
import io.github.kroune.cumobile.data.repository.AuthRepositoryImpl
import io.github.kroune.cumobile.data.repository.CalendarRepositoryImpl
import io.github.kroune.cumobile.data.repository.ContentRepositoryImpl
import io.github.kroune.cumobile.data.repository.CourseRepositoryImpl
import io.github.kroune.cumobile.data.repository.FileRenameRepositoryImpl
import io.github.kroune.cumobile.data.repository.FileRepositoryImpl
import io.github.kroune.cumobile.data.repository.NotificationRepositoryImpl
import io.github.kroune.cumobile.data.repository.PerformanceRepositoryImpl
import io.github.kroune.cumobile.data.repository.ProfileRepositoryImpl
import io.github.kroune.cumobile.data.repository.TaskRepositoryImpl
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import io.github.kroune.cumobile.presentation.main.MainDependencies
import io.github.kroune.cumobile.presentation.root.DefaultRootComponent
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 * Core Koin module providing shared dependencies.
 * Platform-specific modules (e.g., DataStore) are provided via [platformModule].
 */
private val networkModule = module {
    single { createHttpClient() }
    single { ProfileApiService(get()) }
    single { TaskApiService(get()) }
    single { CourseApiService(get()) }
    single { ContentApiService(get()) }
    single { NotificationApiService(get()) }
    single { PerformanceApiService(get()) }
    single { IcalParser() }
    single { IcalApiService(get(), get()) }
    single { TimetableApiService(get()) }
    single { UpdateChecker(get()) }
}

private val dataModule = module {
    single { AuthLocalDataSource(get()) }
    single { CourseLocalDataSource(get()) }
    single { CalendarLocalDataSource(get()) }
    single { get<AppDatabase>().fileRenameRuleDao() }
    single { FileRenameLocalDataSource(get()) }
}

private val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<CourseRepository> { CourseRepositoryImpl(get(), get(), get()) }
    single<ContentRepository> { ContentRepositoryImpl(get(), get()) }
    single<FileRepository> { FileRepositoryImpl(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get()) }
    single<PerformanceRepository> { PerformanceRepositoryImpl(get(), get()) }
    single { GetClassesForDateUseCase() }
    single<CalendarRepository> { CalendarRepositoryImpl(get(), get(), get(), get(), get()) }
    single<FileRenameRepository> { FileRenameRepositoryImpl(get()) }
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

/**
 * Creates a [DefaultRootComponent] with dependencies resolved from Koin.
 *
 * Called from Android [io.github.kroune.cumobile.MainActivity] and iOS Swift entry point.
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
        fileRenameRepository = koin.get<FileRenameRepository>(),
        calendarRepository = koin.get<CalendarRepository>(),
        fileOpener = koin.get<FileOpener>(),
        updateChecker = koin.get<UpdateChecker>(),
        pdfGenerator = koin.get<PdfGenerator>(),
        fileStorage = koin.get<FileStorage>(),
    )
    return DefaultRootComponent(
        componentContext = componentContext,
        authRepository = koin.get<AuthRepository>(),
        mainDependencies = mainDependencies,
    )
}
