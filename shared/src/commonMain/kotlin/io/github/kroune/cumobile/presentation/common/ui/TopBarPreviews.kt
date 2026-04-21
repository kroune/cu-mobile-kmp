package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewTopBarDark() {
    CuMobileTheme(darkTheme = true) {
        TopBar(
            title = "Главная",
            avatarUrl = "https://example.com/avatar.png",
            lateDaysBalance = 5,
            onNotificationsClick = {},
            onProfileClick = {},
            onAvatarRetry = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarLight() {
    CuMobileTheme(darkTheme = false) {
        TopBar(
            title = "Главная",
            avatarUrl = "https://example.com/avatar.png",
            lateDaysBalance = 5,
            onNotificationsClick = {},
            onProfileClick = {},
            onAvatarRetry = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarAvatarLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        TopBar(
            title = "Главная",
            avatarUrl = "",
            lateDaysBalance = null,
            onNotificationsClick = {},
            onProfileClick = {},
            onAvatarRetry = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarAvatarLoadingLight() {
    CuMobileTheme(darkTheme = false) {
        TopBar(
            title = "Главная",
            avatarUrl = "",
            lateDaysBalance = null,
            onNotificationsClick = {},
            onProfileClick = {},
            onAvatarRetry = {},
        )
    }
}
