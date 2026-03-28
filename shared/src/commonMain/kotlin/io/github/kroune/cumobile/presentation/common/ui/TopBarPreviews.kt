@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

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

@Preview
@Composable
private fun PreviewTopBarAvatarLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        TopBar(
            title = "Главная",
            profileInitials = "",
            lateDaysBalance = null,
            onNotificationsClick = {},
            onProfileClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewTopBarAvatarLoadingLight() {
    CuMobileTheme(darkTheme = false) {
        TopBar(
            title = "Главная",
            profileInitials = "",
            lateDaysBalance = null,
            onNotificationsClick = {},
            onProfileClick = {},
        )
    }
}
