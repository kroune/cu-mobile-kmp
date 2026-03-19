package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

/**
 * Reusable detail-screen top bar with a back button and title.
 *
 * Optionally accepts [trailingContent] for extra actions
 * (e.g. a logout button on the profile screen).
 *
 * @param title Screen title.
 * @param onBack Navigates back.
 * @param trailingContent Optional composable rendered at the trailing edge.
 */
@Composable
internal fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = AppTheme.colors.accent,
            )
        }
        Text(
            text = title,
            color = AppTheme.colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        trailingContent?.invoke()
    }
}

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
