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
import io.github.kroune.cumobile.data.model.UpdateInfo
import io.github.kroune.cumobile.presentation.courses.DefaultCoursesComponent
import io.github.kroune.cumobile.presentation.files.DefaultFilesComponent
import io.github.kroune.cumobile.presentation.files.FilesComponent
import io.github.kroune.cumobile.presentation.home.DefaultHomeComponent
import io.github.kroune.cumobile.presentation.home.HomeDependencies
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
@Suppress("TooManyFunctions")
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
                    deps = HomeDependencies(
                        taskRepository = mainDependencies.taskRepository,
                        courseRepository = mainDependencies.courseRepository,
                        profileRepository = mainDependencies.profileRepository,
                        calendarRepository = mainDependencies.calendarRepository,
                    ),
                    onOpenTask = { navigateToCourseDetail(it.course.id) },
                    onOpenCourse = ::navigateToCourseDetail,
                    onOpenProfile = ::navigateToProfile,
                ),
            )
            TabConfig.Tasks -> MainComponent.TabChild.TasksChild(
                DefaultTasksComponent(
                    componentContext = childContext,
                    taskRepository = mainDependencies.taskRepository,
                    onOpenTask = { navigateToCourseDetail(it.course.id) },
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
                    onOpenFile = { path ->
                        mainDependencies.fileOpener.openFile(path)
                    },
                    onOpenRenameSettings = ::navigateToFileRenameSettings,
                    onOpenScanner = ::navigateToScanner,
                ),
            )
        }

    // endregion

    // region Detail navigation (ChildStack)

    private val detailNavigation = StackNavigation<DetailConfig>()

    private val detailChildFactory = DetailChildFactory(
        deps = mainDependencies,
        navigateBack = ::navigateDetailBack,
        navigateToLongread = ::navigateToLongread,
        onLogout = onLogout,
        scope = scope,
        refreshFiles = ::refreshFiles,
    )

    override val detailStack: Value<ChildStack<*, MainComponent.DetailChild>> =
        childStack(
            source = detailNavigation,
            serializer = DetailConfig.serializer(),
            initialConfiguration = DetailConfig.None,
            key = "DetailStack",
            handleBackButton = true,
            childFactory = detailChildFactory::create,
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

    override fun navigateToScanner() {
        detailNavigation.push(DetailConfig.Scanner)
    }

    override fun navigateDetailBack() {
        detailNavigation.pop()
    }

    private fun refreshFiles() {
        val filesItem = tabPages.value.items.getOrNull(FILES_TAB_INDEX)
        val filesChild = filesItem?.instance
        if (filesChild is MainComponent.TabChild.FilesChild) {
            filesChild.component.onIntent(FilesComponent.Intent.Refresh)
        }
    }

    // endregion

    // region Logout

    override fun onLogout() {
        scope.launch {
            onLogout.invoke()
        }
    }

    // endregion

    companion object {
        private const val FILES_TAB_INDEX = 3
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
    internal sealed interface DetailConfig {
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

        @Serializable
        data object Scanner : DetailConfig
    }

    // endregion
}
