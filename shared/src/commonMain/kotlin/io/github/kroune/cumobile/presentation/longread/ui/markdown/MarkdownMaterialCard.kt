package io.github.kroune.cumobile.presentation.longread.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.component.markdown.MarkdownMaterialComponent
import io.github.kroune.cumobile.presentation.longread.htmlrender.HtmlContent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MarkdownMaterialCard(
    material: LongreadMaterial,
    currentState: MarkdownMaterialComponent.State,
) {
    val html = material.viewContent.orEmpty()
    val title = material.contentName
    val blocks = remember(html) {
        if (html.isBlank()) persistentListOf() else parseHtmlToBlocks(html)
    }
    if (title.isNullOrBlank() && blocks.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        title?.let { name ->
            Text(
                text = name,
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (blocks.isNotEmpty()) {
            HtmlContent(
                blocks = blocks,
                searchQuery = currentState.searchQuery,
            )
        }
    }
}
