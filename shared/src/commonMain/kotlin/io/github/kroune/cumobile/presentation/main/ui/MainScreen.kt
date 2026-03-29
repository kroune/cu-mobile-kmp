package io.github.kroune.cumobile.presentation.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.TopBar
import io.github.kroune.cumobile.presentation.courses.detail.ui.CourseDetailScreen
import io.github.kroune.cumobile.presentation.courses.ui.CoursesScreen
import io.github.kroune.cumobile.presentation.files.ui.FilesScreen
import io.github.kroune.cumobile.presentation.home.HomeComponent
import io.github.kroune.cumobile.presentation.home.ui.HomeScreen
import io.github.kroune.cumobile.presentation.longread.ui.LongreadScreen
import io.github.kroune.cumobile.presentation.main.MainComponent
import io.github.kroune.cumobile.presentation.notifications.ui.NotificationsScreen
import io.github.kroune.cumobile.presentation.performance.ui.CoursePerformanceScreen
import io.github.kroune.cumobile.presentation.profile.ui.ProfileScreen
import io.github.kroune.cumobile.presentation.scanner.ui.ScannerScreen
import io.github.kroune.cumobile.presentation.tasks.ui.TasksScreen
import com.arkivanov.decompose.router.pages.ChildPages as PagesState

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
    val updateInfo by component.updateInfo.subscribeAsState()
    val selectedIndex = pages.selectedIndex
    val hasDetail =
        detailStack.active.instance !is MainComponent.DetailChild.None

    // Extract profile initials and late days from the Home tab (observed reactively)
    val homeState = extractHomeState(pages)

    if (updateInfo.latestVersion.isNotEmpty()) {
        UpdateDialog(
            latestVersion = updateInfo.latestVersion,
            releaseName = updateInfo.releaseName,
            onDismiss = { component.dismissUpdate() },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content: top bar + tabs + bottom nav
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            TopBar(
                title = TAB_LABELS[selectedIndex],
                profileInitials = homeState?.profileInitials?.dataOrNull.orEmpty(),
                avatarBytes = homeState?.avatarBytes?.dataOrNull,
                lateDaysBalance = homeState?.lateDaysBalance?.dataOrNull,
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
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = AppTheme.colors.background,
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
                    selectedIconColor = AppTheme.colors.accent,
                    selectedTextColor = AppTheme.colors.accent,
                    unselectedIconColor = AppTheme.colors.textSecondary,
                    unselectedTextColor = AppTheme.colors.textSecondary,
                    indicatorColor = AppTheme.colors.accent.copy(alpha = 0.1f),
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
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
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
            is MainComponent.DetailChild.FileRenameSettingsChild -> {
                io.github.kroune.cumobile.presentation.files.rename.ui.FileRenameSettingsScreen(
                    component = instance.component,
                    onBack = { component.navigateDetailBack() },
                )
            }
            is MainComponent.DetailChild.ScannerChild -> {
                ScannerScreen(
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
 *
 * Uses [subscribeAsState] to observe changes reactively so the
 * top bar updates when profile data finishes loading.
 */
@Composable
private fun extractHomeState(pages: PagesState<*, MainComponent.TabChild>): HomeComponent.State? {
    val homeChild = pages.items.firstOrNull()?.instance
    if (homeChild !is MainComponent.TabChild.HomeChild) return null
    return homeChild.component.state
        .subscribeAsState()
        .value
}

/** Update available dialog. */
@Composable
private fun UpdateDialog(
    latestVersion: String,
    releaseName: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Доступно обновление",
                color = AppTheme.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            if (releaseName.isNotBlank()) {
                Text(
                    text = releaseName,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 14.sp,
                )
            }
            Text(
                text = "Новая версия: $latestVersion",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Позже",
                        color = AppTheme.colors.accent,
                    )
                }
            }
        }
    }
}

/** Bottom nav tab labels. */
private val TAB_LABELS = listOf("Главная", "Задания", "Обучение", "Файлы")

/** Unicode icons for the bottom nav tabs. */
private val TAB_ICONS = listOf("\uD83C\uDFE0", "\uD83D\uDCCB", "\uD83C\uDF93", "\uD83D\uDCC1")
