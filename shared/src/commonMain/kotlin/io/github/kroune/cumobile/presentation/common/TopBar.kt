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
            .background(AppColors.Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Title (left-aligned, takes available space)
        Text(
            text = title,
            color = AppColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        // Late Days balance (optional)
        if (lateDaysBalance != null) {
            Text(
                text = "Late Days: $lateDaysBalance",
                color = AppColors.TextSecondary,
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
            .border(2.dp, AppColors.Accent, CircleShape)
            .background(AppColors.Surface, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            color = AppColors.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
