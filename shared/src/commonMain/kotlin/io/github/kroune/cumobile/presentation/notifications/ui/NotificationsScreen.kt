package io.github.kroune.cumobile.presentation.notifications.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.formatDateTimeFull
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.EmptyContent
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.SegmentedControl
import io.github.kroune.cumobile.presentation.notifications.NotificationsComponent
import kotlinx.collections.immutable.persistentListOf

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
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is NotificationsComponent.Effect.ShowError -> {
                    actionError = effect.message
                }
            }
        }
    }

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
        actionError = actionError,
        onIntent = component::onIntent,
        onBack = onBack,
        onDismissError = { actionError = null },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreenContent(
    state: NotificationsComponent.State,
    actionError: String? = null,
    onIntent: (NotificationsComponent.Intent) -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isContentLoading,
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
                labels = persistentListOf("Учеба", "Другое"),
                selectedIndex = state.selectedTab,
                onSelect = { onIntent(NotificationsComponent.Intent.SelectTab(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ActionErrorBar(error = actionError, onDismiss = onDismissError)

            when (val current = state.currentNotifications) {
                is ContentState.Loading -> NotificationsScreenSkeleton()
                is ContentState.Error -> ErrorContent(
                    error = current.message,
                    onRetry = { onIntent(NotificationsComponent.Intent.Refresh) },
                )
                is ContentState.Success -> {
                    if (current.data.isEmpty()) {
                        EmptyContent(text = "Нет уведомлений")
                    } else {
                        NotificationsList(
                            notifications = current.data,
                            expandedIds = state.expandedNotificationIds,
                            onToggleExpand = { id ->
                                onIntent(NotificationsComponent.Intent.ToggleExpand(id))
                            },
                            onLinkClick = { uri ->
                                onIntent(NotificationsComponent.Intent.OpenLink(uri))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsList(
    notifications: List<NotificationItem>,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
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
            NotificationCard(
                item = item,
                isExpanded = item.id in expandedIds,
                onToggleExpand = { onToggleExpand(item.id) },
                onLinkClick = onLinkClick,
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private const val CollapsedDescriptionMaxLines = 3
private const val CollapsedTitleMaxLines = 2
private const val CollapsedLinkMaxLines = 1

@Composable
private fun NotificationCard(
    item: NotificationItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hasOverflow by remember(item.id) { mutableStateOf(false) }
    val onOverflowDetected = { hasOverflow = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpand)
            .animateContentSize()
            .padding(12.dp),
    ) {
        NotificationIcon(icon = item.icon, category = item.category)
        Spacer(modifier = Modifier.width(12.dp))
        NotificationCardContent(
            item = item,
            isExpanded = isExpanded,
            hasOverflow = hasOverflow,
            onOverflowDetected = onOverflowDetected,
            onLinkClick = onLinkClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotificationCardContent(
    item: NotificationItem,
    isExpanded: Boolean,
    hasOverflow: Boolean,
    onOverflowDetected: () -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        NotificationCardHeader(
            item = item,
            isExpanded = isExpanded,
            onOverflowDetected = onOverflowDetected,
        )
        NotificationCardBody(
            item = item,
            isExpanded = isExpanded,
            onOverflowDetected = onOverflowDetected,
            onLinkClick = onLinkClick,
        )
        if (hasOverflow || isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) "Свернуть" else "Читать полностью",
                color = AppTheme.colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun NotificationCardHeader(
    item: NotificationItem,
    isExpanded: Boolean,
    onOverflowDetected: () -> Unit,
) {
    Text(
        text = item.title,
        color = AppTheme.colors.textPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedTitleMaxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { if (!isExpanded && it.hasVisualOverflow) onOverflowDetected() },
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = formatDateTimeFull(item.createdAt),
        color = AppTheme.colors.textSecondary,
        fontSize = 11.sp,
    )
}

@Composable
private fun NotificationCardBody(
    item: NotificationItem,
    isExpanded: Boolean,
    onOverflowDetected: () -> Unit,
    onLinkClick: (String) -> Unit,
) {
    val descriptionText = normalizeWhitespace(item.description)
    if (descriptionText.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = descriptionText,
            color = AppTheme.colors.textSecondary.copy(alpha = 0.8f),
            fontSize = 12.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedDescriptionMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!isExpanded && it.hasVisualOverflow) onOverflowDetected() },
        )
    }
    val link = item.link
    if (link != null && link.uri.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = link.label.ifBlank { link.uri },
            color = AppTheme.colors.accent,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onLinkClick(link.uri) },
            maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedLinkMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!isExpanded && it.hasVisualOverflow) onOverflowDetected() },
        )
    }
}

/**
 * Trims and collapses excessive blank lines in notification descriptions.
 */
private fun normalizeWhitespace(text: String): String =
    text.trim().replace(Regex("\\n{3,}"), "\n\n")

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

@Composable
private fun NotificationsScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(SkeletonNotificationCount) {
            NotificationCardSkeleton()
        }
    }
}
