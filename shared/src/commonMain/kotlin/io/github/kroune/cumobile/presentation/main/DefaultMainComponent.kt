package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.UpdateInfo
import io.github.kroune.cumobile.presentation.courses.DefaultCoursesComponent
import io.github.kroune.cumobile.presentation.courses.detail.DefaultCourseDetailComponent
import io.github.kroune.cumobile.presentation.files.DefaultFilesComponent
import io.github.kroune.cumobile.presentation.home.DefaultHomeComponent
import io.github.kroune.cumobile.presentation.longread.DefaultLongreadComponent
import io.github.kroune.cumobile.presentation.longread.LongreadParams
import io.github.kroune.cumobile.presentation.notifications.DefaultNotificationsComponent
import io.github.kroune.cumobile.presentation.performance.DefaultCoursePerformanceComponent
import io.github.kroune.cumobile.presentation.profile.DefaultProfileComponent
import io.github.kroune.cumobile.presentation.tasks.DefaultTasksComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Default implementation of [MainComponent].
 *
 * Manages bottom navigation via [ChildPages] and detail navigation
 * via [ChildStack]. Tab components are created lazily and their state
 * is preserved when switching tabs.
 */
@OptIn(DelicateDecomposeApi::class)
class DefaultMainComponent(
    componentContext: ComponentContext,
    private val mainDependencies: MainDependencies,
    private val onLogout: () -> Unit,
) : MainComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    // region Update check

    private val noUpdate = UpdateInfo(latestVersion = "", releasePageUrl = "")
    private val _updateInfo = MutableValue(noUpdate)
    override val updateInfo: Value<UpdateInfo> = _updateInfo

    override fun dismissUpdate() {
        _updateInfo.value = noUpdate
    }

    init {
        scope.launch {
            val info = mainDependencies.updateChecker.checkForUpdate()
            if (info != null) {
                _updateInfo.value = info
            }
        }
    }

    // endregion

    // region Tab navigation (ChildPages)

    private val tabNavigation = PagesNavigation<TabConfig>()

    override val tabPages: Value<ChildPages<*, MainComponent.TabChild>> =
        childPages(
            source = tabNavigation,
            serializer = TabConfig.serializer(),
            initialPages = {
                Pages(
                    items = listOf(
                        TabConfig.Home,
                        TabConfig.Tasks,
                        TabConfig.Courses,
                        TabConfig.Files,
                    ),
                    selectedIndex = 0,
                )
            },
            pageStatus = { index, pages ->
                if (index == pages.selectedIndex) Status.RESUMED else Status.CREATED
            },
            childFactory = ::createTabChild,
        )

    override fun selectTab(index: Int) {
        tabNavigation.select(index)
    }

    private fun createTabChild(
        config: TabConfig,
        childContext: ComponentContext,
    ): MainComponent.TabChild =
        when (config) {
            TabConfig.Home -> MainComponent.TabChild.HomeChild(
                DefaultHomeComponent(
                    componentContext = childContext,
                    taskRepository = mainDependencies.taskRepository,
                    courseRepository = mainDependencies.courseRepository,
                    profileRepository = mainDependencies.profileRepository,
                    calendarRepository = mainDependencies.calendarRepository,
                    onOpenTask = ::handleOpenTask,
                    onOpenCourse = ::navigateToCourseDetail,
                ),
            )
            TabConfig.Tasks -> MainComponent.TabChild.TasksChild(
                DefaultTasksComponent(
                    componentContext = childContext,
                    taskRepository = mainDependencies.taskRepository,
                    onOpenTask = ::handleOpenTask,
                ),
            )
            TabConfig.Courses -> MainComponent.TabChild.CoursesChild(
                DefaultCoursesComponent(
                    componentContext = childContext,
                    courseRepository = mainDependencies.courseRepository,
                    performanceRepository = mainDependencies.performanceRepository,
                    onOpenCourse = ::navigateToCourseDetail,
                    onOpenCoursePerformance = ::navigateToCoursePerformance,
                ),
            )
            TabConfig.Files -> MainComponent.TabChild.FilesChild(
                DefaultFilesComponent(
                    componentContext = childContext,
                    fileRepository = mainDependencies.fileRepository,
                    onOpenRenameSettings = ::navigateToFileRenameSettings,
                ),
            )
        }

    // endregion

    // region Detail navigation (ChildStack)

    private val detailNavigation = StackNavigation<DetailConfig>()

    override val detailStack: Value<ChildStack<*, MainComponent.DetailChild>> =
        childStack(
            source = detailNavigation,
            serializer = DetailConfig.serializer(),
            initialConfiguration = DetailConfig.None,
            key = "DetailStack",
            handleBackButton = true,
            childFactory = ::createDetailChild,
        )

    override fun navigateToProfile() {
        detailNavigation.push(DetailConfig.Profile)
    }

    override fun navigateToNotifications() {
        detailNavigation.push(DetailConfig.Notifications)
    }

    override fun navigateToCourseDetail(courseId: Int) {
        detailNavigation.push(DetailConfig.CourseDetail(courseId))
    }

    override fun navigateToCoursePerformance(
        courseId: Int,
        courseName: String,
        totalGrade: Int,
    ) {
        detailNavigation.push(
            DetailConfig.CoursePerformance(courseId, courseName, totalGrade),
        )
    }

    override fun navigateToLongread(
        longreadId: Int,
        courseId: Int,
        themeId: Int,
    ) {
        detailNavigation.push(
            DetailConfig.Longread(longreadId, courseId, themeId),
        )
    }

    override fun navigateToFileRenameSettings() {
        detailNavigation.push(DetailConfig.FileRenameSettings)
    }

    override fun navigateDetailBack() {
        detailNavigation.pop()
    }

    private fun createDetailChild(
        config: DetailConfig,
        childContext: ComponentContext,
    ): MainComponent.DetailChild =
        when (config) {
            DetailConfig.None -> MainComponent.DetailChild.None
            is DetailConfig.CourseDetail ->
                MainComponent.DetailChild.CourseDetailChild(
                    DefaultCourseDetailComponent(
                        componentContext = childContext,
                        courseId = config.courseId,
                        courseRepository = mainDependencies.courseRepository,
                        onOpenLongread = { longreadId, cId, themeId ->
                            navigateToLongread(longreadId, cId, themeId)
                        },
                        onBack = ::navigateDetailBack,
                    ),
                )
            is DetailConfig.Longread ->
                MainComponent.DetailChild.LongreadChild(
                    DefaultLongreadComponent(
                        componentContext = childContext,
                        params = LongreadParams(
                            longreadId = config.longreadId,
                            courseId = config.courseId,
                            themeId = config.themeId,
                        ),
                        contentRepository = mainDependencies.contentRepository,
                        taskRepository = mainDependencies.taskRepository,
                        renameRepository = mainDependencies.fileRenameRepository,
                        onBack = ::navigateDetailBack,
                        onDownloadReady = { url, filename ->
                            scope.launch {
                                mainDependencies.fileRepository.downloadAndSave(
                                    url,
                                    filename,
                                )
                            }
                        },
                    ),
                )
            is DetailConfig.CoursePerformance ->
                MainComponent.DetailChild.CoursePerformanceChild(
                    DefaultCoursePerformanceComponent(
                        componentContext = childContext,
                        courseId = config.courseId,
                        courseName = config.courseName,
                        totalGrade = config.totalGrade,
                        performanceRepository = mainDependencies.performanceRepository,
                        onBack = ::navigateDetailBack,
                    ),
                )
            DetailConfig.Profile ->
                MainComponent.DetailChild.ProfileChild(
                    DefaultProfileComponent(
                        componentContext = childContext,
                        profileRepository = mainDependencies.profileRepository,
                        onBack = ::navigateDetailBack,
                        onLogout = onLogout,
                    ),
                )
            DetailConfig.Notifications ->
                MainComponent.DetailChild.NotificationsChild(
                    DefaultNotificationsComponent(
                        componentContext = childContext,
                        notificationRepository = mainDependencies.notificationRepository,
                        onBack = ::navigateDetailBack,
                    ),
                )
            DetailConfig.FileRenameSettings ->
                MainComponent.DetailChild.FileRenameSettingsChild(
                    io.github.kroune.cumobile.presentation.files.rename.DefaultFileRenameSettingsComponent(
                        componentContext = childContext,
                        renameRepository = mainDependencies.fileRenameRepository,
                        courseRepository = mainDependencies.courseRepository,
                        onBack = ::navigateDetailBack,
                    ),
                )
        }

    // endregion

    // region Logout

    override fun onLogout() {
        scope.launch {
            onLogout.invoke()
        }
    }

    // endregion

    /**
     * Handles opening a task from the home screen deadlines section.
     *
     * Navigates to the course detail since the task does not carry a
     * longreadId. Direct longread navigation requires resolving through
     * the course overview (deferred to a later phase).
     */
    private fun handleOpenTask(task: StudentTask) {
        navigateToCourseDetail(task.course.id)
    }

    // region Serializable configs

    @Serializable
    private sealed interface TabConfig {
        @Serializable
        data object Home : TabConfig

        @Serializable
        data object Tasks : TabConfig

        @Serializable
        data object Courses : TabConfig

        @Serializable
        data object Files : TabConfig
    }

    @Serializable
    private sealed interface DetailConfig {
        @Serializable
        data object None : DetailConfig

        @Serializable
        data class CourseDetail(
            val courseId: Int,
        ) : DetailConfig

        @Serializable
        data class Longread(
            val longreadId: Int,
            val courseId: Int,
            val themeId: Int,
        ) : DetailConfig

        @Serializable
        data class CoursePerformance(
            val courseId: Int,
            val courseName: String,
            val totalGrade: Int,
        ) : DetailConfig

        @Serializable
        data object Profile : DetailConfig

        @Serializable
        data object Notifications : DetailConfig

        @Serializable
        data object FileRenameSettings : DetailConfig
    }

    // endregion
}
