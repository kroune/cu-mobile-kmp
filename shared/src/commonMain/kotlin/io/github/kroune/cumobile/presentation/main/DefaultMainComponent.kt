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
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.UpdateInfo
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

    private val tabChildFactory = TabChildFactory(
        deps = mainDependencies,
        navigateToCourseDetail = ::navigateToCourseDetail,
        navigateToProfile = ::navigateToProfile,
        navigateToCoursePerformance = ::navigateToCoursePerformance,
        navigateToFileRenameSettings = ::navigateToFileRenameSettings,
        navigateToScanner = ::navigateToScanner,
    )

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
            childFactory = tabChildFactory::create,
        )

    override fun selectTab(index: Int) {
        tabNavigation.select(index)
    }

    init {
        val tabBackCallback = BackCallback(isEnabled = false) {
            tabNavigation.select(index = 0)
        }
        backHandler.register(tabBackCallback)
        tabPages.subscribe { pages ->
            tabBackCallback.isEnabled = pages.selectedIndex != 0
        }
    }

    // endregion

    // region Detail navigation (ChildStack)

    private val detailNavigation = StackNavigation<DetailConfig>()

    private val filesCoordinator = FilesCoordinator(
        tabPages = tabPages,
        selectTab = ::selectTab,
        detailNavigation = detailNavigation,
        scope = scope,
    )

    private val detailChildFactory = DetailChildFactory(
        deps = mainDependencies,
        navigateBack = ::navigateDetailBack,
        navigateToLongread = ::navigateToLongread,
        onLogout = onLogout,
        downloadCallbacks = DownloadCallbacks(
            refreshFiles = filesCoordinator::refreshFiles,
            navigateToFiles = filesCoordinator::navigateToFilesWithHighlight,
            notifyStart = filesCoordinator::notifyDownloadStart,
            notifyComplete = filesCoordinator::notifyDownloadComplete,
        ),
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
        detailNavigation.pushNew(DetailConfig.Profile)
    }

    override fun navigateToNotifications() {
        detailNavigation.pushNew(DetailConfig.Notifications)
    }

    override fun navigateToCourseDetail(courseId: String) {
        detailNavigation.pushNew(DetailConfig.CourseDetail(courseId))
    }

    override fun navigateToCoursePerformance(
        courseId: String,
        courseName: String,
        totalGrade: Int,
    ) {
        detailNavigation.bringToFront(
            DetailConfig.CoursePerformance(courseId, courseName, totalGrade),
        )
    }

    override fun navigateToLongread(
        longreadId: String,
        courseId: String,
        themeId: String,
    ) {
        detailNavigation.pushNew(
            DetailConfig.Longread(longreadId, courseId, themeId),
        )
    }

    override fun navigateToFileRenameSettings() {
        detailNavigation.pushNew(DetailConfig.FileRenameSettings)
    }

    override fun navigateToScanner() {
        detailNavigation.pushNew(DetailConfig.Scanner)
    }

    override fun navigateDetailBack() {
        detailNavigation.pop()
    }

    // endregion

    // region Logout

    override fun onLogout() {
        scope.launch {
            onLogout.invoke()
        }
    }

    // endregion

    // region Serializable configs

    @Serializable
    internal sealed interface TabConfig {
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
            val courseId: String,
        ) : DetailConfig

        @Serializable
        data class Longread(
            val longreadId: String,
            val courseId: String,
            val themeId: String,
        ) : DetailConfig

        @Serializable
        data class CoursePerformance(
            val courseId: String,
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
