package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top bar matching the Flutter reference app.
 *
 * Shows the current tab title, optional Late Days balance,
 * a notification bell, and a profile avatar circle.
 *
 * @param title Current tab label ("Главная", "Задания", etc.)
 * @param profileInitials User's initials (e.g. "ИП") for the avatar.
 * @param lateDaysBalance Late Days balance, shown when non-null.
 * @param onNotificationsClick Navigates to the notifications screen.
 * @param onProfileClick Navigates to the profile screen.
 */
@Composable
fun TopBar(
    title: String,
    profileInitials: String,
    lateDaysBalance: Int?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Title (left-aligned, takes available space)
        Text(
            text = title,
            color = AppTheme.colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        // Late Days balance (optional)
        if (lateDaysBalance != null) {
            Text(
                text = "Late Days: $lateDaysBalance",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Notification bell (text icon – Material Icons Extended unavailable)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onNotificationsClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\uD83D\uDD14", // 🔔
                fontSize = 20.sp,
            )
        }

        // Profile avatar circle
        AvatarCircle(
            initials = profileInitials,
            onClick = onProfileClick,
        )
    }
}

/**
 * Circular avatar showing user initials with a green border.
 *
 * Matches the Flutter reference: 40x40 circle, 2px green border,
 * initials in green on dark background.
 */
@Composable
private fun AvatarCircle(
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(2.dp, AppTheme.colors.accent, CircleShape)
            .background(AppTheme.colors.surface, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            color = AppTheme.colors.accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarDark() {
    CuMobileTheme(darkTheme = true) {
        TopBar(
            title = "Главная",
            profileInitials = "ИП",
            lateDaysBalance = 5,
            onNotificationsClick = {},
            onProfileClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarLight() {
    CuMobileTheme(darkTheme = false) {
        TopBar(
            title = "Главная",
            profileInitials = "ИП",
            lateDaysBalance = 5,
            onNotificationsClick = {},
            onProfileClick = {},
        )
    }
}
