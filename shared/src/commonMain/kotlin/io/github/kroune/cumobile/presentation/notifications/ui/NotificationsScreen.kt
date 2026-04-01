package io.github.kroune.cumobile.presentation.notifications.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.formatDateTimeFull
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTabRow
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.EmptyContent
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.notifications.NotificationsComponent

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

            val pagerState = rememberPagerState(initialPage = state.selectedTab) { 2 }

            LaunchedEffect(state.selectedTab) {
                if (pagerState.currentPage != state.selectedTab) {
                    pagerState.animateScrollToPage(state.selectedTab)
                }
            }

            AppTabRow(
                currentPage = pagerState.currentPage,
                labels = listOf("учеба", "другое"),
                onPageSelected = { page ->
                    onIntent(NotificationsComponent.Intent.SelectTab(page))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ActionErrorBar(error = actionError, onDismiss = onDismissError)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false,
            ) { page ->
                val pageNotifications = when (page) {
                    0 -> state.educationNotifications
                    else -> state.otherNotifications
                }
                when (val current = pageNotifications) {
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
        contentPadding = PaddingValues(vertical = 16.dp),
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
            onLinkClick = onLinkClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotificationCardContent(
    item: NotificationItem,
    isExpanded: Boolean,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Measure text synchronously during composition to detect overflow
    // without a second layout pass / recomposition.
    BoxWithConstraints(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        val maxWidthPx = constraints.maxWidth

        val titleStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        val bodyStyle = TextStyle(fontSize = 12.sp)

        val titleOverflows = remember(item.title, maxWidthPx) {
            textMeasurer
                .measure(
                    text = item.title,
                    style = titleStyle,
                    constraints = Constraints(maxWidth = maxWidthPx),
                    maxLines = CollapsedTitleMaxLines,
                ).hasVisualOverflow
        }

        val descriptionText = remember(item.description) {
            normalizeWhitespace(item.description)
        }
        val descOverflows = remember(descriptionText, maxWidthPx) {
            descriptionText.isNotBlank() &&
                textMeasurer
                    .measure(
                        text = descriptionText,
                        style = bodyStyle,
                        constraints = Constraints(maxWidth = maxWidthPx),
                        maxLines = CollapsedDescriptionMaxLines,
                    ).hasVisualOverflow
        }

        val link = item.link
        val linkText = link?.label?.ifBlank { link.uri }
        val linkOverflows = remember(linkText, link?.uri, maxWidthPx) {
            link?.uri?.isNotBlank() == true &&
                linkText != null &&
                linkText.isNotBlank() &&
                textMeasurer
                    .measure(
                        text = linkText,
                        style = bodyStyle,
                        constraints = Constraints(maxWidth = maxWidthPx),
                        maxLines = CollapsedLinkMaxLines,
                    ).hasVisualOverflow
        }

        val hasOverflow = titleOverflows || descOverflows || linkOverflows

        Column {
            NotificationCardHeader(item = item, isExpanded = isExpanded)
            NotificationCardBody(
                item = item,
                descriptionText = descriptionText,
                isExpanded = isExpanded,
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
}

@Composable
private fun NotificationCardHeader(
    item: NotificationItem,
    isExpanded: Boolean,
) {
    Text(
        text = item.title,
        color = AppTheme.colors.textPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedTitleMaxLines,
        overflow = TextOverflow.Ellipsis,
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
    descriptionText: String,
    isExpanded: Boolean,
    onLinkClick: (String) -> Unit,
) {
    if (descriptionText.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = descriptionText,
            color = AppTheme.colors.textSecondary.copy(alpha = 0.8f),
            fontSize = 12.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedDescriptionMaxLines,
            overflow = TextOverflow.Ellipsis,
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
    val iconVector = notificationIconVector(icon, category)
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.accent.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = AppTheme.colors.accent,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Maps notification icon/category to a Material icon.
 *
 * ServiceDesk → headset, News → newspaper, Education → book, default → bell.
 */
private fun notificationIconVector(
    icon: String,
    category: String,
): androidx.compose.ui.graphics.vector.ImageVector =
    when (icon.lowercase()) {
        "servicedesk" -> Icons.Outlined.Headset
        "news" -> Icons.Outlined.Newspaper
        "education" -> Icons.AutoMirrored.Outlined.MenuBook
        else -> if (category.lowercase().contains("education") || category == "1") {
            Icons.AutoMirrored.Outlined.MenuBook
        } else {
            Icons.Outlined.Notifications
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
