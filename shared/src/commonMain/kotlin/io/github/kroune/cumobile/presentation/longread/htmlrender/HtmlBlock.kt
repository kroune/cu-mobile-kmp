package io.github.kroune.cumobile.presentation.longread.htmlrender

/**
 * Block-level HTML element, rendered as a distinct composable in the content column.
 */
sealed interface HtmlBlock {
    data class Paragraph(
        val inlines: List<InlineElement>,
    ) : HtmlBlock

    data class Heading(
        val level: Int,
        val inlines: List<InlineElement>,
    ) : HtmlBlock

    data class CodeBlock(
        val code: String,
        val language: String?,
    ) : HtmlBlock

    data class BlockQuote(
        val children: List<HtmlBlock>,
    ) : HtmlBlock

    data class UnorderedList(
        val items: List<ListItem>,
    ) : HtmlBlock

    data class OrderedList(
        val items: List<ListItem>,
        val startIndex: Int = 1,
    ) : HtmlBlock

    data class ImageBlock(
        val src: String,
        val alt: String?,
    ) : HtmlBlock

    data class Table(
        val headers: List<List<InlineElement>>,
        val rows: List<List<List<InlineElement>>>,
    ) : HtmlBlock

    data object HorizontalRule : HtmlBlock
}

/** Single list item, which may contain nested blocks (e.g. paragraphs, sub-lists). */
data class ListItem(
    val blocks: List<HtmlBlock>,
)

/**
 * Inline-level HTML element, composed into [androidx.compose.ui.text.AnnotatedString].
 */
sealed interface InlineElement {
    data class Text(
        val text: String,
    ) : InlineElement

    data class Bold(
        val children: List<InlineElement>,
    ) : InlineElement

    data class Italic(
        val children: List<InlineElement>,
    ) : InlineElement

    data class Code(
        val text: String,
    ) : InlineElement

    data class Link(
        val href: String,
        val children: List<InlineElement>,
    ) : InlineElement

    data object LineBreak : InlineElement
}
