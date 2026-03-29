package io.github.kroune.cumobile.presentation.longread.htmlrender

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import kotlinx.collections.immutable.ImmutableList

/**
 * Converts a list of [InlineElement] into an [AnnotatedString] with proper
 * styling for bold, italic, code, and links. Optionally applies search
 * highlighting.
 */
@Composable
fun buildInlineAnnotatedString(
    inlines: ImmutableList<InlineElement>,
    searchQuery: String = "",
): AnnotatedString {
    val accentColor = AppTheme.colors.accent
    val codeBg = AppTheme.colors.codeBlockBackground
    val textColor = AppTheme.colors.textPrimary

    return buildAnnotatedString {
        appendInlines(inlines, accentColor, codeBg, textColor)
        if (searchQuery.isNotBlank()) {
            applySearchHighlight(this, searchQuery, accentColor.copy(alpha = 0.3f))
        }
    }
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: ImmutableList<InlineElement>,
    accentColor: Color,
    codeBg: Color,
    textColor: Color,
) {
    for (element in inlines) {
        when (element) {
            is InlineElement.Text -> append(element.text)
            is InlineElement.Bold -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendInlines(element.children, accentColor, codeBg, textColor)
                pop()
            }
            is InlineElement.Italic -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendInlines(element.children, accentColor, codeBg, textColor)
                pop()
            }
            is InlineElement.Code -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBg,
                        color = textColor,
                    ),
                )
                append(element.text)
                pop()
            }
            is InlineElement.Link -> {
                val link = LinkAnnotation.Url(resolveUrl(element.href))
                pushStyle(
                    SpanStyle(
                        color = accentColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                )
                pushLink(link)
                appendInlines(element.children, accentColor, codeBg, textColor)
                pop() // link
                pop() // style
            }
            is InlineElement.LineBreak -> append("\n")
        }
    }
}

/**
 * Overlays search highlight spans on all case-insensitive matches of [query].
 */
private fun applySearchHighlight(
    builder: AnnotatedString.Builder,
    query: String,
    highlightColor: Color,
) {
    val text = builder.toAnnotatedString().text.lowercase()
    val lowerQuery = query.lowercase()
    val style = SpanStyle(background = highlightColor)
    var startIndex = 0
    while (true) {
        val index = text.indexOf(lowerQuery, startIndex)
        if (index < 0) break
        builder.addStyle(style, index, index + query.length)
        startIndex = index + 1
    }
}
