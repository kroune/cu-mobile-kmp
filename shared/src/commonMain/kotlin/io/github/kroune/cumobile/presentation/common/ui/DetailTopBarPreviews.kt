package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewDetailTopBarDark() {
    CuMobileTheme(darkTheme = true) {
        DetailTopBar(title = "Задание", onBack = {})
    }
}

@Preview
@Composable
private fun PreviewDetailTopBarLight() {
    CuMobileTheme(darkTheme = false) {
        DetailTopBar(title = "Задание", onBack = {})
    }
}
