package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.presentation.courses.DefaultCoursesComponent
import io.github.kroune.cumobile.presentation.files.DefaultFilesComponent
import io.github.kroune.cumobile.presentation.home.DefaultHomeComponent
import io.github.kroune.cumobile.presentation.home.HomeDependencies
import io.github.kroune.cumobile.presentation.tasks.DefaultTasksComponent

/**
 * Bundle of navigation callbacks used by the tab children.
 * Grouped to keep [TabChildFactory]'s constructor small.
 */
internal data class TabNavigationCallbacks(
    val toCourseDetail: (courseId: String) -> Unit,
    val toTask: (courseId: String, exerciseId: String) -> Unit,
    val toProfile: () -> Unit,
    val toCoursePerformance: (courseId: String, courseName: String, totalGrade: Int) -> Unit,
    val toFileRenameSettings: () -> Unit,
    val toScanner: () -> Unit,
)

/**
 * Factory that creates [MainComponent.TabChild] instances
 * for each tab configuration.
 */
internal class TabChildFactory(
    private val deps: MainDependencies,
    private val nav: TabNavigationCallbacks,
) {
    fun create(
        config: DefaultMainComponent.TabConfig,
        childContext: ComponentContext,
    ): MainComponent.TabChild =
        when (config) {
            DefaultMainComponent.TabConfig.Home -> MainComponent.TabChild.HomeChild(
                DefaultHomeComponent(
                    componentContext = childContext,
                    deps = HomeDependencies(
                        taskRepository = deps.taskRepository,
                        courseRepository = deps.courseRepository,
                        profileRepository = deps.profileRepository,
                        calendarRepository = deps.calendarRepository,
                    ),
                    onOpenTask = { nav.toTask(it.course.id, it.exercise.id) },
                    onOpenCourse = nav.toCourseDetail,
                    onOpenProfile = nav.toProfile,
                ),
            )
            DefaultMainComponent.TabConfig.Tasks -> MainComponent.TabChild.TasksChild(
                DefaultTasksComponent(
                    componentContext = childContext,
                    taskRepository = deps.taskRepository,
                    dispatchers = deps.dispatchers,
                    onOpenTask = { nav.toTask(it.course.id, it.exercise.id) },
                ),
            )
            DefaultMainComponent.TabConfig.Courses -> MainComponent.TabChild.CoursesChild(
                DefaultCoursesComponent(
                    componentContext = childContext,
                    courseRepository = deps.courseRepository,
                    performanceRepository = deps.performanceRepository,
                    onOpenCourse = nav.toCourseDetail,
                    onOpenCoursePerformance = nav.toCoursePerformance,
                ),
            )
            DefaultMainComponent.TabConfig.Files -> MainComponent.TabChild.FilesChild(
                DefaultFilesComponent(
                    componentContext = childContext,
                    fileRepository = deps.fileRepository,
                    onOpenFile = { path -> deps.fileOpener().openFile(path) },
                    onOpenRenameSettings = nav.toFileRenameSettings,
                    onOpenScanner = nav.toScanner,
                ),
            )
        }
}
