package io.github.kroune.cumobile.di

import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CourseLocalDataSource
import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.local.db.AppDatabase
import io.github.kroune.cumobile.data.network.AuthApiService
import io.github.kroune.cumobile.data.network.ContentApiService
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.data.network.NotificationApiService
import io.github.kroune.cumobile.data.network.PerformanceApiService
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.data.network.QuizApiService
import io.github.kroune.cumobile.data.network.ResettableCookieStorage
import io.github.kroune.cumobile.data.network.TaskApiService
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.data.network.UpdateChecker
import io.github.kroune.cumobile.data.network.createAuthHttpClient
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
import io.github.kroune.cumobile.data.repository.QuizRepositoryImpl
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
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import io.github.kroune.cumobile.presentation.main.MainDependencies
import io.github.kroune.cumobile.presentation.root.DefaultRootComponent
import io.github.kroune.cumobile.util.AppDispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Core Koin module providing shared dependencies.
 * Platform-specific modules (e.g., DataStore) are provided via [platformModule].
 */
private val coreModule = module {
    single { AppDispatchers() }
}

private val networkModule = module {
    single { createHttpClient(get()) }
    single { ResettableCookieStorage() }
    single(named("auth")) { createAuthHttpClient(get()) }
    single { AuthApiService(inject(named("auth")), inject()) }
    single { ProfileApiService(inject()) }
    single { TaskApiService(inject()) }
    single { CourseApiService(inject()) }
    single { ContentApiService(inject()) }
    single { NotificationApiService(inject()) }
    single { PerformanceApiService(inject()) }
    single { TimetableApiService(inject()) }
    single { QuizApiService(inject()) }
    single { UpdateChecker(inject()) }
}

private val dataModule = module {
    single { AuthLocalDataSource(inject()) }
    single { CourseLocalDataSource(inject()) }
    single { get<AppDatabase>().fileRenameRuleDao() }
    single { FileRenameLocalDataSource(inject()) }
}

private val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(inject(), inject(), inject()) }
    single<ProfileRepository> { ProfileRepositoryImpl(inject(), inject(), inject()) }
    single<TaskRepository> { TaskRepositoryImpl(inject(), inject(), inject()) }
    single<CourseRepository> { CourseRepositoryImpl(inject(), inject(), inject(), inject()) }
    single<ContentRepository> { ContentRepositoryImpl(inject(), inject(), inject()) }
    single<FileRepository> { FileRepositoryImpl(inject(), inject(), inject()) }
    single<NotificationRepository> { NotificationRepositoryImpl(inject(), inject(), inject()) }
    single<PerformanceRepository> { PerformanceRepositoryImpl(inject(), inject(), inject()) }
    single<QuizRepository> { QuizRepositoryImpl(inject(), inject(), inject()) }
    single { GetClassesForDateUseCase() }
    single<CalendarRepository> { CalendarRepositoryImpl(inject(), inject(), inject(), inject()) }
    single<FileRenameRepository> { FileRenameRepositoryImpl(inject()) }
    single {
        MainDependencies(
            taskRepository = inject(),
            courseRepository = inject(),
            profileRepository = inject(),
            performanceRepository = inject(),
            contentRepository = inject(),
            notificationRepository = inject(),
            fileRepository = inject(),
            fileRenameRepository = inject(),
            calendarRepository = inject(),
            quizRepository = inject(),
            fileOpener = inject(),
            updateChecker = inject(),
            pdfGenerator = inject(),
            fileStorage = inject(),
            dispatchers = inject(),
        )
    }
}

/**
 * Initializes Koin with common modules and an optional platform-specific module.
 * @param platformModule platform-specific module providing DataStore, etc.
 */
fun initKoin(platformModule: Module = module { }) {
    startKoin {
        modules(
            platformModule,
            coreModule,
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
 *
 * Uses `KoinComponent.inject()` so the repositories / dependency bundles behind
 * [Lazy] wrappers aren't materialised until the root component or a child that
 * owns the Lazy actually accesses them.
 */
fun createRootComponent(componentContext: ComponentContext): DefaultRootComponent =
    RootComponentFactory.create(componentContext)

private object RootComponentFactory : KoinComponent {
    fun create(componentContext: ComponentContext): DefaultRootComponent {
        val authRepository: Lazy<AuthRepository> = inject()
        val mainDependencies: Lazy<MainDependencies> = inject()
        val authApiService: Lazy<AuthApiService> = inject()
        return DefaultRootComponent(
            componentContext = componentContext,
            authRepository = authRepository,
            authApiService = authApiService,
            mainDependencies = mainDependencies,
        )
    }
}
