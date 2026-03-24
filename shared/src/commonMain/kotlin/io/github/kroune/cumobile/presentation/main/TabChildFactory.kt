package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.presentation.courses.DefaultCoursesComponent
import io.github.kroune.cumobile.presentation.files.DefaultFilesComponent
import io.github.kroune.cumobile.presentation.home.DefaultHomeComponent
import io.github.kroune.cumobile.presentation.home.HomeDependencies
import io.github.kroune.cumobile.presentation.tasks.DefaultTasksComponent

/**
 * Factory that creates [MainComponent.TabChild] instances
 * for each tab configuration.
 */
internal class TabChildFactory(
    private val deps: MainDependencies,
    private val navigateToCourseDetail: (String) -> Unit,
    private val navigateToProfile: () -> Unit,
    private val navigateToCoursePerformance: (String, String, Int) -> Unit,
    private val navigateToFileRenameSettings: () -> Unit,
    private val navigateToScanner: () -> Unit,
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
                    onOpenTask = { navigateToCourseDetail(it.course.id) },
                    onOpenCourse = navigateToCourseDetail,
                    onOpenProfile = navigateToProfile,
                ),
            )
            DefaultMainComponent.TabConfig.Tasks -> MainComponent.TabChild.TasksChild(
                DefaultTasksComponent(
                    componentContext = childContext,
                    taskRepository = deps.taskRepository,
                    onOpenTask = { navigateToCourseDetail(it.course.id) },
                ),
            )
            DefaultMainComponent.TabConfig.Courses -> MainComponent.TabChild.CoursesChild(
                DefaultCoursesComponent(
                    componentContext = childContext,
                    courseRepository = deps.courseRepository,
                    performanceRepository = deps.performanceRepository,
                    onOpenCourse = navigateToCourseDetail,
                    onOpenCoursePerformance = navigateToCoursePerformance,
                ),
            )
            DefaultMainComponent.TabConfig.Files -> MainComponent.TabChild.FilesChild(
                DefaultFilesComponent(
                    componentContext = childContext,
                    fileRepository = deps.fileRepository,
                    onOpenFile = { path ->
                        deps.fileOpener.openFile(path)
                    },
                    onOpenRenameSettings = navigateToFileRenameSettings,
                    onOpenScanner = navigateToScanner,
                ),
            )
        }
}
