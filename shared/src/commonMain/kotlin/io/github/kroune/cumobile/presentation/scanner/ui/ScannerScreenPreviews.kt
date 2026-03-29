package io.github.kroune.cumobile.presentation.scanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.scanner.ScannerComponent

@Preview
@Composable
private fun PreviewScannerEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        ScannerScreenContent(
            state = ScannerComponent.State(fileName = "Скан_20.03.2026_14-30"),
            actionError = null,
            imagePicker = null,
            onIntent = {},
            onBack = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewScannerEmptyLight() {
    CuMobileTheme(darkTheme = false) {
        ScannerScreenContent(
            state = ScannerComponent.State(fileName = "Скан_20.03.2026_14-30"),
            actionError = null,
            imagePicker = null,
            onIntent = {},
            onBack = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewScannerSavingDark() {
    CuMobileTheme(darkTheme = true) {
        ScannerScreenContent(
            state = ScannerComponent.State(fileName = "Скан_20.03.2026_14-30", isSaving = true),
            actionError = null,
            imagePicker = null,
            onIntent = {},
            onBack = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewScannerErrorDark() {
    CuMobileTheme(darkTheme = true) {
        ScannerScreenContent(
            state = ScannerComponent.State(fileName = "Скан_20.03.2026_14-30"),
            actionError = "Не удалось создать PDF",
            imagePicker = null,
            onIntent = {},
            onBack = {},
            onDismissError = {},
        )
    }
}
