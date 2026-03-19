package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

/**
 * Full-screen centered loading indicator.
 */
@Composable
internal fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}

/**
 * Full-screen centered error message with an optional retry button.
 *
 * @param error The error message to display.
 * @param onRetry Callback for the retry button; when `null` the button is hidden.
 */
@Composable
internal fun ErrorContent(
    error: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                color = AppTheme.colors.error,
                fontSize = 14.sp,
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onRetry) {
                    Text(text = "Повторить", color = AppTheme.colors.accent)
                }
            }
        }
    }
}

/**
 * Full-screen centered empty-state placeholder text.
 *
 * @param text The message to display (e.g. "Нет данных").
 */
@Composable
internal fun EmptyContent(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
    }
}

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
