package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = AppTheme.colors.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = AppTheme.colors.textSecondary,
                fontSize = 16.sp,
            )
        }
    }
}

/**
 * Inline error bar for transient action errors (e.g. mutation failures).
 *
 * @param error Error message to display; when `null` the bar is hidden.
 * @param onDismiss Callback to clear the error.
 */
@Composable
internal fun ActionErrorBar(
    error: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (error == null) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.error.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = AppTheme.colors.error,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = error,
            color = AppTheme.colors.error,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) {
            Text(
                text = "\u2715",
                color = AppTheme.colors.error,
                fontSize = 14.sp,
            )
        }
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
