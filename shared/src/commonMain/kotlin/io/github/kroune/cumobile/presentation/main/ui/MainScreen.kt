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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.ChildPagesPager
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.baseline.BaselineTestTags
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.TopBar
import io.github.kroune.cumobile.presentation.courses.detail.ui.CourseDetailScreen
import io.github.kroune.cumobile.presentation.courses.ui.CoursesScreen
import io.github.kroune.cumobile.presentation.files.ui.FilesScreen
import io.github.kroune.cumobile.presentation.home.ui.HomeScreen
import io.github.kroune.cumobile.presentation.longread.ui.LongreadScreen
import io.github.kroune.cumobile.presentation.main.MainComponent
import io.github.kroune.cumobile.presentation.notifications.ui.NotificationsScreen
import io.github.kroune.cumobile.presentation.performance.ui.CoursePerformanceScreen
import io.github.kroune.cumobile.presentation.profile.ui.ProfileScreen
import io.github.kroune.cumobile.presentation.scanner.ui.ScannerScreen
import io.github.kroune.cumobile.presentation.tasks.ui.TasksScreen

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
    val updateInfo by component.updateInfo.subscribeAsState()
    val topBarState by component.topBarState.subscribeAsState()
    val selectedIndex = pages.selectedIndex

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
                avatarUrl = topBarState.avatarUrl,
                lateDaysBalance = topBarState.lateDaysBalance.dataOrNull,
                onNotificationsClick = { component.navigateToNotifications() },
                onProfileClick = { component.navigateToProfile() },
                onAvatarRetry = { component.onAvatarChanged() },
            )

            ChildPages(
                pages = component.tabPages,
                onPageSelected = { component.selectTab(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                scrollAnimation = PagesScrollAnimation.Default,
                pager = noSwipePager(),
            ) { _, tabChild ->
                TabContent(tabChild)
            }

            BottomNavBar(
                selectedIndex = selectedIndex,
                onTabSelected = { component.selectTab(it) },
            )
        }

        // Detail overlay (always composed so exit animations can play)
        DetailOverlay(component = component)
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
                modifier = Modifier.testTag(TAB_TAGS[index]),
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = TAB_ICONS[index],
                        contentDescription = label,
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
        animation = stackAnimation(scale(frontFactor = 0.9f, backFactor = 1f) + fade()),
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

/** Material icons for the bottom nav tabs. */
private val TAB_ICONS = listOf(
    Icons.Outlined.Home,
    Icons.AutoMirrored.Outlined.ListAlt,
    Icons.Outlined.School,
    Icons.Outlined.Folder,
)

/** Baseline-profile anchor tags, parallel to [TAB_LABELS]. */
private val TAB_TAGS = listOf(
    BaselineTestTags.TAB_HOME,
    BaselineTestTags.TAB_TASKS,
    BaselineTestTags.TAB_COURSES,
    BaselineTestTags.TAB_FILES,
)

/** Pager that disables user swiping between tabs. */
private fun noSwipePager(): ChildPagesPager =
    { modifier, state, key, pageContent ->
        HorizontalPager(
            modifier = modifier,
            state = state,
            key = key,
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top,
            pageContent = pageContent,
        )
    }
