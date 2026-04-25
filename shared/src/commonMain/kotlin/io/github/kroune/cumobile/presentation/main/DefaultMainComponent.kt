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
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.UpdateInfo
import io.github.kroune.cumobile.data.network.ApiEndpoints
import io.github.kroune.cumobile.data.network.BaseUrl
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock.System

private val logger = KotlinLogging.logger {}

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
    private val mainDependenciesLazy: Lazy<MainDependencies>,
    private val onLogout: () -> Unit,
) : MainComponent,
    ComponentContext by componentContext {
    private val scope = componentScope()

    // region Update check

    private val noUpdate = UpdateInfo(latestVersion = "", releasePageUrl = "")
    private val _updateInfo = MutableValue(noUpdate)
    override val updateInfo: Value<UpdateInfo> = _updateInfo

    override fun dismissUpdate() {
        _updateInfo.value = noUpdate
    }

    init {
        scope.launch {
            val info = mainDependenciesLazy().updateChecker().checkForUpdate()
            if (info != null) {
                _updateInfo.value = info
            }
        }
    }

    // endregion

    // region Top bar state (profile header shared across all tabs)

    private var avatarVersion = System.now().toEpochMilliseconds()

    private val _topBarState = MutableValue(
        TopBarState(avatarUrl = buildAvatarUrl()),
    )
    override val topBarState: Value<TopBarState> = _topBarState

    init {
        scope.launch {
            loadLateDaysBalance()
        }
    }

    private suspend fun loadLateDaysBalance() {
        runCatchingCancellable {
            mainDependenciesLazy().profileRepository().fetchLmsProfile()
        }.fold(
            onSuccess = { lmsProfile ->
                _topBarState.value = _topBarState.value.copy(
                    lateDaysBalance = if (lmsProfile != null) {
                        ContentState.Success(lmsProfile.lateDaysBalance)
                    } else {
                        ContentState.Error("Не удалось загрузить баланс")
                    },
                )
            },
            onFailure = { e ->
                logger.error(e) { "Failed to load late days balance" }
                _topBarState.value = _topBarState.value.copy(
                    lateDaysBalance = ContentState.Error("Не удалось загрузить баланс"),
                )
            },
        )
    }

    private fun buildAvatarUrl(): String =
        "${BaseUrl}${ApiEndpoints.Profile.AVATAR_ME}?v=$avatarVersion"

    override fun onAvatarChanged() {
        avatarVersion = System.now().toEpochMilliseconds()
        _topBarState.value = _topBarState.value.copy(avatarUrl = buildAvatarUrl())
    }

    // endregion

    // region Navigation

    private val tabNavigation = PagesNavigation<TabConfig>()
    private val detailNavigation = StackNavigation<DetailConfig>()

    override val navigation = object : MainComponent.Navigation {
        override fun toProfile() {
            detailNavigation.pushNew(DetailConfig.Profile)
        }

        override fun toNotifications() {
            detailNavigation.pushNew(DetailConfig.Notifications)
        }

        override fun toCourseDetail(courseId: String) {
            detailNavigation.pushNew(DetailConfig.CourseDetail(courseId))
        }

        override fun toCoursePerformance(
            courseId: String,
            courseName: String,
            totalGrade: Int,
        ) {
            detailNavigation.bringToFront(
                DetailConfig.CoursePerformance(courseId, courseName, totalGrade),
            )
        }

        override fun toLongread(
            longreadId: String,
            courseId: String,
            themeId: String,
        ) {
            detailNavigation.pushNew(
                DetailConfig.Longread(longreadId, courseId, themeId),
            )
        }

        override fun toFileRenameSettings() {
            detailNavigation.pushNew(DetailConfig.FileRenameSettings)
        }

        override fun toScanner() {
            detailNavigation.pushNew(DetailConfig.Scanner)
        }

        override fun back() {
            detailNavigation.pop()
        }
    }

    private fun navigateToTask(task: StudentTask) {
        val longreadId = task.longread.id
        val themeId = task.theme.id
        val courseId = task.course.id
        if (longreadId.isBlank() || themeId.isBlank()) {
            logger.error {
                "Cannot navigate to task ${task.id}: longreadId=$longreadId, themeId=$themeId"
            }
            return
        }
        detailNavigation.pushNew(
            DetailConfig.Longread(longreadId, courseId, themeId, focusTaskId = task.id),
        )
    }

    // endregion

    // region Tab navigation (ChildPages)

    private val tabChildFactory = TabChildFactory(
        deps = mainDependenciesLazy(),
        nav = TabNavigationCallbacks(
            toCourseDetail = navigation::toCourseDetail,
            toTask = ::navigateToTask,
            toCoursePerformance = navigation::toCoursePerformance,
            toFileRenameSettings = navigation::toFileRenameSettings,
            toScanner = navigation::toScanner,
        ),
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
                if (index == pages.selectedIndex) Status.RESUMED else Status.DESTROYED
            },
            childFactory = tabChildFactory::create,
        )

    override fun selectTab(index: Int) {
        tabNavigation.select(index)
    }

    init {
        val tabBackCallback = BackCallback(isEnabled = false) {
            tabNavigation.select(index = HOME_TAB_INDEX)
        }
        backHandler.register(tabBackCallback)
        tabPages.subscribe { pages ->
            tabBackCallback.isEnabled = pages.selectedIndex != HOME_TAB_INDEX
        }
    }

    // endregion

    // region Detail navigation (ChildStack)

    private val filesCoordinator = FilesCoordinator(
        tabPages = tabPages,
        selectTab = ::selectTab,
        detailNavigation = detailNavigation,
        scope = scope,
    )

    private val detailChildFactory = DetailChildFactory(
        deps = mainDependenciesLazy(),
        navigateBack = navigation::back,
        navigateToLongread = navigation::toLongread,
        onLogout = onLogout,
        onAvatarChanged = ::onAvatarChanged,
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
            val focusTaskId: String? = null,
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

    private companion object {
        const val HOME_TAB_INDEX = 0
    }
}
