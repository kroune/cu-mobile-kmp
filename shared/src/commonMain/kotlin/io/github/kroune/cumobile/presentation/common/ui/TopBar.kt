package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage

@Composable
fun TopBar(
    title: String,
    avatarUrl: String,
    lateDaysBalance: Int?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAvatarRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = AppTheme.colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        if (lateDaysBalance != null) {
            Text(
                text = "Late Days: $lateDaysBalance",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onNotificationsClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Уведомления",
                tint = AppTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        AvatarCircle(
            avatarUrl = avatarUrl,
            onClick = onProfileClick,
            onRetry = onAvatarRetry,
        )
    }
}

@Composable
private fun AvatarCircle(
    avatarUrl: String,
    onClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (avatarUrl.isEmpty()) {
        ShimmerCircle(
            size = 40.dp,
            modifier = modifier.clickable(onClick = onClick),
        )
        return
    }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(2.dp, AppTheme.colors.accent, CircleShape)
            .background(AppTheme.colors.surface, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = "Аватар",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                ShimmerCircle(size = 40.dp)
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onRetry),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Повторить",
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}
