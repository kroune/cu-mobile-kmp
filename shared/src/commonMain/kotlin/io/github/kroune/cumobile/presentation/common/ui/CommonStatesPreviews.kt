package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background)) {
            LoadingContent()
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background)) {
            LoadingContent()
        }
    }
}

@Preview
@Composable
private fun PreviewErrorDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background)) {
            ErrorContent(error = "Ошибка загрузки", onRetry = {})
        }
    }
}

@Preview
@Composable
private fun PreviewErrorLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background)) {
            ErrorContent(error = "Ошибка загрузки", onRetry = {})
        }
    }
}

@Preview
@Composable
private fun PreviewEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background)) {
            EmptyContent(text = "Нет данных")
        }
    }
}

@Preview
@Composable
private fun PreviewEmptyLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background)) {
            EmptyContent(text = "Нет данных")
        }
    }
}

@Preview
@Composable
private fun PreviewActionErrorBarDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background)) {
            ActionErrorBar(
                error = "Не удалось выполнить действие",
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewActionErrorBarLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background)) {
            ActionErrorBar(
                error = "Не удалось выполнить действие",
                onDismiss = {},
            )
        }
    }
}
