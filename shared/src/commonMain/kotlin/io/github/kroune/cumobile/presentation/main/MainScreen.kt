package io.github.kroune.cumobile.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.TopBar
import io.github.kroune.cumobile.presentation.courses.CoursesScreen
import io.github.kroune.cumobile.presentation.courses.detail.CourseDetailScreen
import io.github.kroune.cumobile.presentation.files.FilesScreen
import io.github.kroune.cumobile.presentation.home.HomeScreen
import io.github.kroune.cumobile.presentation.longread.LongreadScreen
import io.github.kroune.cumobile.presentation.notifications.NotificationsScreen
import io.github.kroune.cumobile.presentation.performance.CoursePerformanceScreen
import io.github.kroune.cumobile.presentation.profile.ProfileScreen
import io.github.kroune.cumobile.presentation.tasks.TasksScreen

/**
 * Main screen with bottom navigation and top bar.
 *
 * Renders:
 * 1. Top bar with title, late days, notifications, and profile avatar.
 * 2. Tab content via [ChildPages] (Home, Tasks, Courses, Files).
 * 3. Bottom navigation bar with 4 tabs.
 * 4. Detail overlay via [Children] stack (CourseDetail, Profile, Notifications).
 */
@Composable
fun MainScreen(component: MainComponent) {
    val pages by component.tabPages.subscribeAsState()
    val detailStack by component.detailStack.subscribeAsState()
    val selectedIndex = pages.selectedIndex
    val hasDetail =
        detailStack.active.instance !is MainComponent.DetailChild.None

    // Extract profile initials and late days from the Home tab
    val homeState = extractHomeState(pages)

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content: top bar + tabs + bottom nav
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background),
        ) {
            TopBar(
                title = TAB_LABELS[selectedIndex],
                profileInitials = homeState?.profileInitials.orEmpty(),
                lateDaysBalance = homeState?.lateDaysBalance,
                onNotificationsClick = { component.navigateToNotifications() },
                onProfileClick = { component.navigateToProfile() },
            )

            ChildPages(
                pages = component.tabPages,
                onPageSelected = { component.selectTab(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { _, tabChild ->
                TabContent(tabChild)
            }

            BottomNavBar(
                selectedIndex = selectedIndex,
                onTabSelected = { component.selectTab(it) },
            )
        }

        // Detail overlay (renders on top of everything)
        if (hasDetail) {
            DetailOverlay(component = component)
        }
    }
}

@Composable
private fun TabContent(child: MainComponent.TabChild) {
    when (child) {
        is MainComponent.TabChild.HomeChild -> HomeScreen(child.component)
        is MainComponent.TabChild.TasksChild -> TasksScreen(child.component)
        is MainComponent.TabChild.CoursesChild -> CoursesScreen(child.component)
        is MainComponent.TabChild.FilesChild -> FilesScreen(child.component)
    }
}

/**
 * Bottom navigation bar with 4 tabs using text icons.
 *
 * Matches the Flutter reference: dark background, green selected,
 * grey unselected.
 */
@Composable
private fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = AppColors.Background,
    ) {
        TAB_LABELS.forEachIndexed { index, label ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Text(
                        text = TAB_ICONS[index],
                        fontSize = 20.sp,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Accent,
                    selectedTextColor = AppColors.Accent,
                    unselectedIconColor = AppColors.TextSecondary,
                    unselectedTextColor = AppColors.TextSecondary,
                    indicatorColor = AppColors.Accent.copy(alpha = 0.1f),
                ),
            )
        }
    }
}

@Composable
private fun DetailOverlay(
    component: MainComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.detailStack,
        modifier = modifier.fillMaxSize(),
    ) { child ->
        when (val instance = child.instance) {
            MainComponent.DetailChild.None -> {
                // No detail screen to display
            }
            is MainComponent.DetailChild.CourseDetailChild -> {
                CourseDetailScreen(
                    component = instance.component,
                    onBack = { component.navigateDetailBack() },
                )
            }
            is MainComponent.DetailChild.LongreadChild -> {
                LongreadScreen(
                    component = instance.component,
                )
            }
            is MainComponent.DetailChild.CoursePerformanceChild -> {
                CoursePerformanceScreen(
                    component = instance.component,
                    onBack = { component.navigateDetailBack() },
                )
            }
            is MainComponent.DetailChild.ProfileChild -> {
                ProfileScreen(
                    component = instance.component,
                    onBack = { component.navigateDetailBack() },
                )
            }
            is MainComponent.DetailChild.NotificationsChild -> {
                NotificationsScreen(
                    component = instance.component,
                    onBack = { component.navigateDetailBack() },
                )
            }
        }
    }
}

/**
 * Extracts the HomeComponent state from the tab pages
 * for displaying profile initials and late days in the top bar.
 */
private fun extractHomeState(
    pages: com.arkivanov.decompose.router.pages.ChildPages<
        *,
        MainComponent.TabChild,
    >,
): io.github.kroune.cumobile.presentation.home.HomeComponent.State? {
    val homeChild = pages.items.firstOrNull()?.instance
    return if (homeChild is MainComponent.TabChild.HomeChild) {
        homeChild.component.state.value
    } else {
        null
    }
}

/** Bottom nav tab labels. */
private val TAB_LABELS = listOf("Главная", "Задания", "Обучение", "Файлы")

/** Unicode icons for the bottom nav tabs. */
private val TAB_ICONS = listOf("\uD83C\uDFE0", "\uD83D\uDCCB", "\uD83C\uDF93", "\uD83D\uDCC1")
