package io.github.kroune.cumobile.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.data.model.NotificationLink
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.NotificationCardSkeleton
import io.github.kroune.cumobile.presentation.common.SegmentedControl
import io.github.kroune.cumobile.presentation.common.formatDateTimeFull

/**
 * Notifications screen with two tabs: "Учеба" and "Другое".
 *
 * Layout:
 * 1. Top bar with back button and title
 * 2. Tab selector (Education / Other)
 * 3. Notification cards list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    component: NotificationsComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val uriHandler = LocalUriHandler.current

    // Open external links when the component signals one
    val externalLink = state.externalLinkToOpen
    LaunchedEffect(externalLink) {
        if (externalLink != null) {
            val fullUrl = when {
                externalLink.startsWith("http://") || externalLink.startsWith("https://") ->
                    externalLink
                externalLink.startsWith("/") ->
                    "https://my.centraluniversity.ru$externalLink"
                else -> "https://my.centraluniversity.ru/$externalLink"
            }
            uriHandler.openUri(fullUrl)
            component.onIntent(NotificationsComponent.Intent.ExternalLinkOpened)
        }
    }

    NotificationsScreenContent(
        state = state,
        onIntent = component::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreenContent(
    state: NotificationsComponent.State,
    onIntent: (NotificationsComponent.Intent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onIntent(NotificationsComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            DetailTopBar(
                title = "Уведомления",
                onBack = onBack,
            )

            SegmentedControl(
                labels = listOf("Учеба", "Другое"),
                selectedIndex = state.selectedTab,
                onSelect = { onIntent(NotificationsComponent.Intent.SelectTab(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                state.isLoading && state.currentNotifications.isEmpty() ->
                    NotificationsScreenSkeleton()
                state.error != null && state.currentNotifications.isEmpty() -> ErrorContent(
                    error = state.error,
                    onRetry = { onIntent(NotificationsComponent.Intent.Refresh) },
                )
                state.currentNotifications.isEmpty() -> EmptyContent(
                    text = "Нет уведомлений",
                )
                else -> NotificationsList(
                    notifications = state.currentNotifications,
                    onLinkClick = { uri ->
                        onIntent(NotificationsComponent.Intent.OpenLink(uri))
                    },
                )
            }
        }
    }
}

@Composable
private fun NotificationsList(
    notifications: List<NotificationItem>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(notifications, key = { it.id }) { item ->
            NotificationCard(item = item, onLinkClick = onLinkClick)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        // Category icon
        NotificationIcon(icon = item.icon, category = item.category)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title
            Text(
                text = item.title,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Date
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDateTimeFull(item.createdAt),
                color = AppTheme.colors.textSecondary,
                fontSize = 11.sp,
            )

            // Description
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description.trim(),
                    color = AppTheme.colors.textSecondary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Link
            val link = item.link
            if (link != null && link.uri.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = link.label.ifBlank { link.uri },
                    color = AppTheme.colors.accent,
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onLinkClick(link.uri) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NotificationIcon(
    icon: String,
    category: String,
    modifier: Modifier = Modifier,
) {
    val emoji = notificationIconEmoji(icon, category)
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.accent.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

/**
 * Maps notification icon/category to an emoji.
 *
 * Matches the Flutter reference: ServiceDesk → headphones,
 * News → newspaper, Education → book, default → bell.
 */
private fun notificationIconEmoji(
    icon: String,
    category: String,
): String =
    when (icon.lowercase()) {
        "servicedesk" -> "\uD83C\uDFA7" // 🎧
        "news" -> "\uD83D\uDCF0" // 📰
        "education" -> "\uD83D\uDCDA" // 📚
        else -> if (category.lowercase().contains("education") || category == "1") {
            "\uD83D\uDCDA" // 📚
        } else {
            "\uD83D\uDD14" // 🔔
        }
    }

private const val SkeletonNotificationCount = 4
private val SkeletonNotificationSpacing = 8.dp
private val SkeletonNotificationPadding = 16.dp

/**
 * Skeleton loading state for the Notifications screen.
 *
 * Shows shimmer placeholder cards matching the notification list layout.
 * The [SegmentedControl] is already rendered above the when-block.
 */
@Composable
private fun NotificationsScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SkeletonNotificationPadding),
        verticalArrangement = Arrangement.spacedBy(SkeletonNotificationSpacing),
    ) {
        repeat(SkeletonNotificationCount) {
            NotificationCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

private val previewNotificationsState = NotificationsComponent.State(
    educationNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "Новое задание",
            description = "Преподаватель назначил задание по курсу Алгоритмы",
            icon = "education",
            category = "1",
            createdAt = "2026-03-18T10:00:00",
        ),
        NotificationItem(
            id = "2",
            title = "Оценка выставлена",
            description = "Получена оценка 8 за ДЗ: Деревья",
            icon = "education",
            category = "1",
            createdAt = "2026-03-17T14:30:00",
        ),
    ),
)

@Preview
@Composable
private fun PreviewNotificationsScreenDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewNotificationsState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = previewNotificationsState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(isLoading = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsErrorDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                error = "Не удалось загрузить уведомления",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsErrorLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                error = "Не удалось загрузить уведомления",
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

private val previewOtherTabState = NotificationsComponent.State(
    selectedTab = 1,
    otherNotifications = listOf(
        NotificationItem(
            id = "10",
            title = "Обновление системы",
            description = "Плановое техническое обслуживание 25 марта с 02:00 до 06:00",
            icon = "news",
            category = "2",
            createdAt = "2026-03-20T09:00:00",
        ),
        NotificationItem(
            id = "11",
            title = "Новый опрос",
            description = "Пожалуйста, заполните опрос удовлетворённости обучением",
            icon = "servicedesk",
            category = "2",
            createdAt = "2026-03-19T16:00:00",
            link = NotificationLink(
                uri = "https://example.com/survey",
                label = "Пройти опрос",
            ),
        ),
    ),
)

@Preview
@Composable
private fun PreviewNotificationsOtherTabDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewOtherTabState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsWithLinkDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewNotificationsState.copy(
                educationNotifications = previewNotificationsState.educationNotifications +
                    NotificationItem(
                        id = "3",
                        title = "Новый лонгрид по Алгоритмам",
                        description = "Добавлен материал «Динамическое программирование»",
                        icon = "education",
                        category = "1",
                        createdAt = "2026-03-16T08:00:00",
                        link = NotificationLink(
                            uri = "/longread/123",
                            label = "Открыть лонгрид",
                        ),
                    ),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
