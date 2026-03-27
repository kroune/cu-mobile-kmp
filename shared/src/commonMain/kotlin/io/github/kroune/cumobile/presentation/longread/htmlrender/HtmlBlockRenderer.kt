package io.github.kroune.cumobile.presentation.longread.htmlrender

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.AppTheme

private const val BaseUrl = "https://my.centraluniversity.ru"

private val HeadingFontSizes = mapOf(
    1 to 24,
    2 to 20,
    3 to 18,
    4 to 16,
)
private const val HeadingDefaultFontSize = 15

/**
 * Renders a list of [HtmlBlock] elements as native Compose UI.
 * This is the main entry point for HTML content rendering.
 */
@Composable
fun HtmlContent(
    blocks: List<HtmlBlock>,
    searchQuery: String = "",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (block in blocks) {
            RenderBlock(block = block, searchQuery = searchQuery)
        }
    }
}

@Composable
private fun RenderBlock(
    block: HtmlBlock,
    searchQuery: String,
) {
    when (block) {
        is HtmlBlock.Paragraph -> ParagraphBlock(block, searchQuery)
        is HtmlBlock.Heading -> HeadingBlock(block, searchQuery)
        is HtmlBlock.CodeBlock -> CodeBlockComposable(block)
        is HtmlBlock.BlockQuote -> BlockQuoteComposable(block, searchQuery)
        is HtmlBlock.UnorderedList -> UnorderedListBlock(block, searchQuery)
        is HtmlBlock.OrderedList -> OrderedListBlock(block, searchQuery)
        is HtmlBlock.ImageBlock -> HtmlImageBlock(block)
        is HtmlBlock.Table -> TableBlock(block, searchQuery)
        is HtmlBlock.HorizontalRule -> HorizontalDivider(
            color = AppTheme.colors.textSecondary.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun ParagraphBlock(
    block: HtmlBlock.Paragraph,
    searchQuery: String,
) {
    val annotated = buildInlineAnnotatedString(block.inlines, searchQuery)
    if (annotated.text.isBlank()) return

    BasicText(
        text = annotated,
        style = androidx.compose.ui.text.TextStyle(
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
    )
}

@Composable
private fun HeadingBlock(
    block: HtmlBlock.Heading,
    searchQuery: String,
) {
    val fontSize = (HeadingFontSizes[block.level] ?: HeadingDefaultFontSize).sp
    val annotated = buildInlineAnnotatedString(block.inlines, searchQuery)
    Text(
        text = annotated,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.textPrimary,
        lineHeight = fontSize * 1.3f,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun CodeBlockComposable(block: HtmlBlock.CodeBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.codeBlockBackground)
            .padding(12.dp),
    ) {
        block.language?.let { lang ->
            Text(
                text = lang,
                color = AppTheme.colors.textSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text = block.code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun BlockQuoteComposable(
    block: HtmlBlock.BlockQuote,
    searchQuery: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
            .background(AppTheme.colors.blockquoteBackground),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(IntrinsicSize.Max)
                .background(AppTheme.colors.accent),
        )
        HtmlContent(
            blocks = block.children,
            searchQuery = searchQuery,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun TableBlock(
    block: HtmlBlock.Table,
    searchQuery: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.codeBlockBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (block.headers.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (cell in block.headers) {
                    Text(
                        text = buildInlineAnnotatedString(cell, searchQuery),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AppTheme.colors.textPrimary,
                    )
                }
            }
            HorizontalDivider(color = AppTheme.colors.textSecondary.copy(alpha = 0.3f))
        }
        for (row in block.rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (cell in row) {
                    Text(
                        text = buildInlineAnnotatedString(cell, searchQuery),
                        fontSize = 13.sp,
                        color = AppTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

internal fun resolveUrl(url: String): String {
    val value = url.trim()
    return when {
        value.startsWith("http://") || value.startsWith("https://") -> value
        value.startsWith("//") -> "https:$value"
        value.startsWith("mailto:") ||
            value.startsWith("tel:") ||
            value.startsWith("data:") ||
            value.startsWith("#") -> value
        value.startsWith("/") -> "$BaseUrl$value"
        else -> "$BaseUrl/$value"
    }
}
