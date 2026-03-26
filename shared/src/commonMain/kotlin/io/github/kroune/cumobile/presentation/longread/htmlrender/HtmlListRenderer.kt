@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread.htmlrender

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.AppTheme

/** Renders an unordered (bulleted) list. */
@Composable
internal fun UnorderedListBlock(
    block: HtmlBlock.UnorderedList,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (item in block.items) {
            ListItemRow(
                prefix = "\u2022",
                item = item,
                searchQuery = searchQuery,
            )
        }
    }
}

/** Renders an ordered (numbered) list. */
@Composable
internal fun OrderedListBlock(
    block: HtmlBlock.OrderedList,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        block.items.forEachIndexed { index, item ->
            ListItemRow(
                prefix = "${block.startIndex + index}.",
                item = item,
                searchQuery = searchQuery,
            )
        }
    }
}

@Composable
private fun ListItemRow(
    prefix: String,
    item: ListItem,
    searchQuery: String,
) {
    Row {
        Text(
            text = prefix,
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier
                .widthIn(min = 20.dp)
                .padding(end = 6.dp),
        )
        HtmlContent(
            blocks = item.blocks,
            searchQuery = searchQuery,
            modifier = Modifier.weight(1f),
        )
    }
}
