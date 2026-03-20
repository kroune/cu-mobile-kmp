package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.presentation.courses.detail.DefaultCourseDetailComponent
import io.github.kroune.cumobile.presentation.files.rename.DefaultFileRenameSettingsComponent
import io.github.kroune.cumobile.presentation.longread.DefaultLongreadComponent
import io.github.kroune.cumobile.presentation.longread.LongreadDependencies
import io.github.kroune.cumobile.presentation.longread.LongreadParams
import io.github.kroune.cumobile.presentation.notifications.DefaultNotificationsComponent
import io.github.kroune.cumobile.presentation.performance.DefaultCoursePerformanceComponent
import io.github.kroune.cumobile.presentation.profile.DefaultProfileComponent
import io.github.kroune.cumobile.presentation.scanner.DefaultScannerComponent
/**
 * Callbacks for file download tracking within detail screens.
 */
internal class DownloadCallbacks(
    val refreshFiles: () -> Unit,
    val navigateToFiles: () -> Unit,
    val notifyStart: (filename: String) -> Unit,
    val notifyComplete: (filename: String) -> Unit,
)

/**
 * Factory that creates [MainComponent.DetailChild] instances
 * for each [DefaultMainComponent.DetailConfig].
 */
internal class DetailChildFactory(
    private val deps: MainDependencies,
    private val navigateBack: () -> Unit,
    private val navigateToLongread: (Int, Int, Int) -> Unit,
    private val onLogout: () -> Unit,
    private val downloadCallbacks: DownloadCallbacks,
) {
    fun create(
        config: DefaultMainComponent.DetailConfig,
        childContext: ComponentContext,
    ): MainComponent.DetailChild =
        when (config) {
            DefaultMainComponent.DetailConfig.None ->
                MainComponent.DetailChild.None
            is DefaultMainComponent.DetailConfig.CourseDetail ->
                createCourseDetail(config, childContext)
            is DefaultMainComponent.DetailConfig.Longread ->
                createLongread(config, childContext)
            is DefaultMainComponent.DetailConfig.CoursePerformance ->
                createCoursePerformance(config, childContext)
            DefaultMainComponent.DetailConfig.Profile ->
                createProfile(childContext)
            DefaultMainComponent.DetailConfig.Notifications ->
                createNotifications(childContext)
            DefaultMainComponent.DetailConfig.FileRenameSettings ->
                createFileRenameSettings(childContext)
            DefaultMainComponent.DetailConfig.Scanner ->
                createScanner(childContext)
        }

    private fun createCourseDetail(
        config: DefaultMainComponent.DetailConfig.CourseDetail,
        childContext: ComponentContext,
    ) =
        MainComponent.DetailChild.CourseDetailChild(
            DefaultCourseDetailComponent(
                componentContext = childContext,
                courseId = config.courseId,
                courseRepository = deps.courseRepository,
                onOpenLongread = navigateToLongread,
                onBack = navigateBack,
            ),
        )

    private fun createLongread(
        config: DefaultMainComponent.DetailConfig.Longread,
        childContext: ComponentContext,
    ) =
        MainComponent.DetailChild.LongreadChild(
            DefaultLongreadComponent(
                componentContext = childContext,
                params = LongreadParams(
                    longreadId = config.longreadId,
                    courseId = config.courseId,
                    themeId = config.themeId,
                ),
                deps = LongreadDependencies(
                    contentRepository = deps.contentRepository,
                    taskRepository = deps.taskRepository,
                    renameRepository = deps.fileRenameRepository,
                ),
                onBack = navigateBack,
                onDownloadReady = { url, filename ->
                    downloadCallbacks.notifyStart(filename)
                    val saved = deps.fileRepository.downloadAndSave(url, filename)
                    downloadCallbacks.notifyComplete(filename)
                    if (saved) downloadCallbacks.refreshFiles()
                    saved
                },
                onNavigateToFiles = downloadCallbacks.navigateToFiles,
            ),
        )

    private fun createCoursePerformance(
        config: DefaultMainComponent.DetailConfig.CoursePerformance,
        childContext: ComponentContext,
    ) =
        MainComponent.DetailChild.CoursePerformanceChild(
            DefaultCoursePerformanceComponent(
                componentContext = childContext,
                courseId = config.courseId,
                courseName = config.courseName,
                totalGrade = config.totalGrade,
                performanceRepository = deps.performanceRepository,
                onBack = navigateBack,
            ),
        )

    private fun createProfile(childContext: ComponentContext) =
        MainComponent.DetailChild.ProfileChild(
            DefaultProfileComponent(
                componentContext = childContext,
                profileRepository = deps.profileRepository,
                calendarRepository = deps.calendarRepository,
                onBack = navigateBack,
                onLogout = onLogout,
            ),
        )

    private fun createNotifications(childContext: ComponentContext) =
        MainComponent.DetailChild.NotificationsChild(
            DefaultNotificationsComponent(
                componentContext = childContext,
                notificationRepository = deps.notificationRepository,
                onBack = navigateBack,
                onOpenLongread = navigateToLongread,
            ),
        )

    private fun createFileRenameSettings(childContext: ComponentContext) =
        MainComponent.DetailChild.FileRenameSettingsChild(
            DefaultFileRenameSettingsComponent(
                componentContext = childContext,
                renameRepository = deps.fileRenameRepository,
                courseRepository = deps.courseRepository,
                onBack = navigateBack,
            ),
        )

    private fun createScanner(childContext: ComponentContext) =
        MainComponent.DetailChild.ScannerChild(
            DefaultScannerComponent(
                componentContext = childContext,
                pdfGenerator = deps.pdfGenerator,
                fileStorage = deps.fileStorage,
                onBack = navigateBack,
                onFileSaved = downloadCallbacks.refreshFiles,
            ),
        )
}
