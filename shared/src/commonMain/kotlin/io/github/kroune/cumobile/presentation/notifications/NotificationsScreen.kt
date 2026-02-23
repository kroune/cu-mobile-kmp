@file:Suppress("TooManyFunctions", "MagicNumber")

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.EmptyContent
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
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
@Composable
fun NotificationsScreen(
    component: NotificationsComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        DetailTopBar(
            title = "Уведомления",
            onBack = onBack,
        )

        SegmentedControl(
            labels = listOf("Учеба", "Другое"),
            selectedIndex = state.selectedTab,
            onSelect = {
                component.onIntent(NotificationsComponent.Intent.SelectTab(it))
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            state.isLoading -> LoadingContent()
            state.error != null && state.currentNotifications.isEmpty() -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { component.onIntent(NotificationsComponent.Intent.Refresh) },
            )
            state.currentNotifications.isEmpty() -> EmptyContent(
                text = "Нет уведомлений",
            )
            else -> NotificationsList(
                notifications = state.currentNotifications,
                onLinkClick = {
                    component.onIntent(NotificationsComponent.Intent.OpenLink(it))
                },
            )
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
            .background(AppColors.Surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        // Category icon
        NotificationIcon(icon = item.icon, category = item.category)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title
            Text(
                text = item.title,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Date
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDateTimeFull(item.createdAt),
                color = AppColors.TextSecondary,
                fontSize = 11.sp,
            )

            // Description
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description.trim(),
                    color = AppColors.TextSecondary.copy(alpha = 0.8f),
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
                    color = AppColors.Accent,
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
            .background(AppColors.Accent.copy(alpha = 0.15f), CircleShape),
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
