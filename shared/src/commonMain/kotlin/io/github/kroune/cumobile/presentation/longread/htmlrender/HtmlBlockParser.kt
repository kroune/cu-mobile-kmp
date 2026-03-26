@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread.htmlrender

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

private val BlockTags = setOf(
    "p",
    "div",
    "h1",
    "h2",
    "h3",
    "h4",
    "h5",
    "h6",
    "ul",
    "ol",
    "li",
    "blockquote",
    "pre",
    "hr",
    "table",
    "figure",
    "section",
    "article",
    "header",
    "footer",
)

private val ParagraphLikeTags = setOf("p", "div", "section", "article", "header", "footer")

/** Parses an HTML string into a list of renderable [HtmlBlock] elements. */
fun parseHtmlToBlocks(html: String): List<HtmlBlock> =
    try {
        val body = Ksoup.parse(html).body()
        parseChildBlocks(body)
    } catch (e: Exception) {
        logger.warn(e) { "Failed to parse HTML to blocks" }
        listOf(HtmlBlock.Paragraph(listOf(InlineElement.Text(html))))
    }

/** Extracts plain text from HTML (for search indexing). */
fun extractPlainText(html: String): String =
    try {
        Ksoup.parse(html).body().text()
    } catch (e: Exception) {
        logger.warn(e) { "Failed to extract plain text from HTML" }
        html
    }

/**
 * Parses all child nodes of [parent] into a list of blocks.
 * Consecutive inline nodes are merged into a single [HtmlBlock.Paragraph].
 */
internal fun parseChildBlocks(parent: Element): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    val pendingInlines = mutableListOf<InlineElement>()

    fun flushInlines() {
        if (pendingInlines.isNotEmpty()) {
            blocks.add(HtmlBlock.Paragraph(pendingInlines.toList()))
            pendingInlines.clear()
        }
    }

    for (node in parent.childNodes()) {
        when {
            node is TextNode -> {
                val text = node.getWholeText()
                if (text.isNotBlank()) {
                    pendingInlines.add(InlineElement.Text(collapseWhitespace(text)))
                }
            }
            node is Element && node.tagName().lowercase() in BlockTags -> {
                flushInlines()
                parseBlockElement(node)?.let { blocks.add(it) }
            }
            node is Element -> {
                pendingInlines.addAll(parseInlineElement(node))
            }
        }
    }
    flushInlines()
    return blocks
}

/** Maps a block-level element to the corresponding [HtmlBlock]. */
internal fun parseBlockElement(element: Element): HtmlBlock? {
    val tag = element.tagName().lowercase()
    return when (tag) {
        in ParagraphLikeTags -> parseParagraphLike(element)
        "h1", "h2", "h3", "h4", "h5", "h6" -> parseHeading(element, tag)
        "pre" -> parseCodeBlock(element)
        "blockquote" -> HtmlBlock.BlockQuote(parseChildBlocks(element))
        "ul" -> parseUnorderedList(element)
        "ol" -> parseOrderedList(element)
        "hr" -> HtmlBlock.HorizontalRule
        "table" -> parseTable(element)
        "figure" -> parseFigure(element)
        else -> parseParagraphLike(element)
    }
}

private fun parseHeading(
    element: Element,
    tag: String,
): HtmlBlock {
    val level = tag.removePrefix("h").toIntOrNull() ?: 1
    return HtmlBlock.Heading(level, parseInlineContent(element))
}

private fun parseParagraphLike(element: Element): HtmlBlock? {
    val children = parseChildBlocks(element)
    if (children.isEmpty()) {
        val inlines = parseInlineContent(element)
        return if (inlines.isEmpty()) null else HtmlBlock.Paragraph(inlines)
    }
    return if (children.size == 1) children.first() else null
}

private fun parseCodeBlock(element: Element): HtmlBlock {
    val codeElement = element.selectFirst("code")
    val code = codeElement?.wholeText() ?: element.wholeText()
    val language = codeElement
        ?.classNames()
        ?.firstOrNull { it.startsWith("language-") }
        ?.removePrefix("language-")
    return HtmlBlock.CodeBlock(code, language)
}

internal fun collapseWhitespace(text: String): String =
    text.replace(Regex("\\s+"), " ")
