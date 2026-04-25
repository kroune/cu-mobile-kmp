package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.UpdateInfo
import io.github.kroune.cumobile.presentation.courses.CoursesComponent
import io.github.kroune.cumobile.presentation.courses.detail.CourseDetailComponent
import io.github.kroune.cumobile.presentation.files.FilesComponent
import io.github.kroune.cumobile.presentation.files.rename.FileRenameSettingsComponent
import io.github.kroune.cumobile.presentation.home.HomeComponent
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.kroune.cumobile.presentation.notifications.NotificationsComponent
import io.github.kroune.cumobile.presentation.performance.CoursePerformanceComponent
import io.github.kroune.cumobile.presentation.profile.ProfileComponent
import io.github.kroune.cumobile.presentation.scanner.ScannerComponent
import io.github.kroune.cumobile.presentation.tasks.TasksComponent

/**
 * Component managing the main screen with bottom navigation.
 *
 * Uses [ChildPages] for the 4 bottom tabs (preserving state on switch)
 * and [ChildStack] for detail screens pushed from any tab.
 *
 * Navigation graph:
 * ```
 * MainComponent
 * ├── Tab pages (ChildPages):
 * │   ├── HomeComponent  (Tab 0 — "Главная")
 * │   ├── TasksComponent (Tab 1 — "Задания")
 * │   ├── CoursesComponent (Tab 2 — "Обучение")
 * │   └── FilesComponent (Tab 3 — "Файлы")
 * └── Detail stack (ChildStack):
 *     ├── CourseDetailChild(courseId)
 *     ├── ProfileChild
 *     └── NotificationsChild
 * ```
 */
interface MainComponent {
    /** Bottom navigation tab pages. */
    val tabPages: Value<ChildPages<*, TabChild>>

    /** Detail navigation stack (overlays tabs when non-empty). */
    val detailStack: Value<ChildStack<*, DetailChild>>

    /** Profile header data displayed in the top bar across all tabs. */
    val topBarState: Value<TopBarState>

    /** Available update info (empty latestVersion if none). */
    val updateInfo: Value<UpdateInfo>

    /** Called when the avatar has been changed (uploaded/deleted) in ProfileScreen. */
    fun onAvatarChanged()

    /** Dismiss the update dialog. */
    fun dismissUpdate()

    /** Select a bottom navigation tab by index. */
    fun selectTab(index: Int)

    /** Detail screen navigation actions. */
    val navigation: Navigation

    /** Called when the user logs out. */
    fun onLogout()

    interface Navigation {
        fun toProfile()

        fun toNotifications()

        fun toCourseDetail(courseId: String)

        fun toLongread(
            longreadId: String,
            courseId: String,
            themeId: String,
        )

        fun toCoursePerformance(
            courseId: String,
            courseName: String,
            totalGrade: Int,
        )

        fun toFileRenameSettings()

        fun toScanner()

        fun back()
    }

    /** Bottom navigation tab children. */
    sealed class TabChild {
        class HomeChild(
            val component: HomeComponent,
        ) : TabChild()

        class TasksChild(
            val component: TasksComponent,
        ) : TabChild()

        class CoursesChild(
            val component: CoursesComponent,
        ) : TabChild()

        class FilesChild(
            val component: FilesComponent,
        ) : TabChild()
    }

    /**
     * Detail screen children pushed on top of tabs.
     *
     * [None] is the default (no detail screen visible).
     * Placeholder children will be replaced with real components
     * in later phases (Phase 8, etc.).
     */
    sealed class DetailChild {
        data object None : DetailChild()

        data class CourseDetailChild(
            val component: CourseDetailComponent,
        ) : DetailChild()

        data class LongreadChild(
            val component: LongreadComponent,
        ) : DetailChild()

        data class CoursePerformanceChild(
            val component: CoursePerformanceComponent,
        ) : DetailChild()

        data class ProfileChild(
            val component: ProfileComponent,
        ) : DetailChild()

        data class NotificationsChild(
            val component: NotificationsComponent,
        ) : DetailChild()

        data class FileRenameSettingsChild(
            val component: FileRenameSettingsComponent,
        ) : DetailChild()

        data class ScannerChild(
            val component: ScannerComponent,
        ) : DetailChild()
    }
}
