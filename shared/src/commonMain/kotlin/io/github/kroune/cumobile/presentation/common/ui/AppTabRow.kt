package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable tab row with the app's standard styling.
 *
 * @param currentPage currently selected tab index
 * @param labels tab labels to display
 * @param onPageSelected callback when a tab is tapped
 * @param modifier optional modifier (e.g. padding)
 * @param containerColor background of the unselected tab row
 */
@Composable
fun AppTabRow(
    currentPage: Int,
    labels: List<String>,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = AppTheme.colors.surface,
) {
    SecondaryTabRow(
        selectedTabIndex = currentPage,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        containerColor = containerColor,
        contentColor = TabRowDefaults.primaryContentColor,
        indicator = {},
        divider = { },
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = currentPage == index
            Tab(
                selected = isSelected,
                onClick = { onPageSelected(index) },
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) {
                            AppTheme.colors.accent.copy(alpha = 0.2f)
                        } else {
                            containerColor
                        },
                    ),
                selectedContentColor = AppTheme.colors.accent,
                unselectedContentColor = AppTheme.colors.textSecondary,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontSize = 14.sp,
                )
            }
        }
    }
}
