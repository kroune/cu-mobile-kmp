package io.github.kroune.cumobile.presentation.longread.htmlrender

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Parses inline content of an element into a flat list of [InlineElement]. */
internal fun parseInlineContent(element: Element): ImmutableList<InlineElement> {
    val result = mutableListOf<InlineElement>()
    for (node in element.childNodes()) {
        result.addAll(parseInlineNode(node))
    }
    return result.toImmutableList()
}

private fun parseInlineNode(node: Node): ImmutableList<InlineElement> =
    when (node) {
        is TextNode -> {
            val text = collapseWhitespace(node.getWholeText())
            if (text.isNotEmpty()) persistentListOf(InlineElement.Text(text)) else persistentListOf()
        }

        is Element -> parseInlineElement(node)
        else -> persistentListOf()
    }

internal fun parseInlineElement(element: Element): ImmutableList<InlineElement> {
    val tag = element.tagName().lowercase()
    return when (tag) {
        "strong", "b" -> persistentListOf(InlineElement.Bold(parseInlineContent(element)))
        "em", "i" -> persistentListOf(InlineElement.Italic(parseInlineContent(element)))
        "code" -> persistentListOf(InlineElement.Code(element.text()))
        "a" -> parseLink(element)
        "br" -> persistentListOf(InlineElement.LineBreak)
        "img" -> parseInlineImage(element)
        else -> parseInlineContent(element)
    }
}

private fun parseLink(element: Element): ImmutableList<InlineElement> =
    persistentListOf(
        InlineElement.Link(
            href = element.attr("href"),
            children = parseInlineContent(element),
        ),
    )

private fun parseInlineImage(element: Element): ImmutableList<InlineElement> {
    val src = element.attr("src")
    val alt = element.attr("alt").ifBlank { null }
    return if (src.isNotBlank()) {
        persistentListOf(InlineElement.Text(alt ?: "[image]"))
    } else {
        persistentListOf()
    }
}

internal fun parseUnorderedList(element: Element): HtmlBlock.UnorderedList {
    val items = element
        .children()
        .filter { it.tagName().lowercase() == "li" }
        .map { ListItem(parseChildBlocks(it)) }
        .toImmutableList()
    return HtmlBlock.UnorderedList(items)
}

internal fun parseOrderedList(element: Element): HtmlBlock.OrderedList {
    val start = element.attr("start").toIntOrNull() ?: 1
    val items = element
        .children()
        .filter { it.tagName().lowercase() == "li" }
        .map { ListItem(parseChildBlocks(it)) }
        .toImmutableList()
    return HtmlBlock.OrderedList(items, start)
}

internal fun parseTable(element: Element): HtmlBlock.Table {
    val headerRow = element.select("thead tr").firstOrNull()
    val headers = headerRow
        ?.select("th,td")
        ?.map { parseInlineContent(it) }
        .orEmpty()
        .toImmutableList()

    val bodyRows = element.select("tbody tr").ifEmpty { element.select("tr") }
    val rows = bodyRows
        .filter { it != headerRow }
        .map { row -> row.select("td,th").map { parseInlineContent(it) }.toImmutableList() }
        .toImmutableList()

    return HtmlBlock.Table(headers, rows)
}

internal fun parseFigure(element: Element): HtmlBlock? {
    val img = element.selectFirst("img")
    return if (img != null) {
        HtmlBlock.ImageBlock(img.attr("src"), img.attr("alt").ifBlank { null })
    } else {
        null
    }
}
