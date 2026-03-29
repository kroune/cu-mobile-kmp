package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
private fun PreviewSegmentedControlDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            SegmentedControl(
                labels = persistentListOf("Курсы", "Ведомость", "Зачётка"),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSegmentedControlLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            SegmentedControl(
                labels = persistentListOf("Курсы", "Ведомость", "Зачётка"),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}
