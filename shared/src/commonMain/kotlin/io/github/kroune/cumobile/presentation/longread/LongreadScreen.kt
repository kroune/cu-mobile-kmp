@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.formatSizeBytes

/**
 * Main longread screen displaying materials within a longread.
 *
 * Renders different card types based on material discriminator:
 * - markdown: HTML content (stripped to plain text)
 * - file: download card with filename and size
 * - coding: task management card (delegated to [CodingMaterialCard])
 * - questions: unsupported placeholder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongreadScreen(
    component: LongreadComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { component.onIntent(LongreadComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            DetailTopBar(
                title = state.title,
                onBack = { component.onIntent(LongreadComponent.Intent.Back) },
            )

            when {
                state.isLoading && state.materials.isEmpty() -> LoadingContent()
                state.error != null && state.materials.isEmpty() -> ErrorContent(
                    error = state.error.orEmpty(),
                    onRetry = { component.onIntent(LongreadComponent.Intent.Refresh) },
                )
                else -> MaterialList(
                    state = state,
                    onIntent = component::onIntent,
                )
            }
        }
    }
}

@Composable
private fun MaterialList(
    state: LongreadComponent.State,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            vertical = 12.dp,
        ),
    ) {
        items(state.materials, key = { it.id }) { material ->
            when {
                material.isMarkdown -> MarkdownCard(material)
                material.isFile -> FileCard(material, onIntent)
                material.isCoding -> CodingMaterialCard(
                    material = material,
                    state = state,
                    onIntent = onIntent,
                )
                material.isQuestions -> QuestionsCard(material)
                else -> MarkdownCard(material)
            }
        }
    }
}

/** Markdown material: displays stripped HTML content as plain text. */
@Composable
private fun MarkdownCard(
    material: LongreadMaterial,
    modifier: Modifier = Modifier,
) {
    val content = material.viewContent?.let { stripHtmlTags(it) }.orEmpty()
    if (content.isBlank()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        material.contentName?.let { name ->
            Text(
                text = name,
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = content,
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

/** File material: shows filename, size, and download button. */
@Composable
private fun FileCard(
    material: LongreadMaterial,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filename = material.filename ?: "Файл"
    val version = material.version.orEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\uD83D\uDCC4 $filename",
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            material.length?.let { size ->
                Text(
                    text = formatSizeBytes(size.toLong()),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        if (version.isNotEmpty()) {
            TextButton(
                onClick = {
                    onIntent(
                        LongreadComponent.Intent.DownloadFile(material),
                    )
                },
            ) {
                Text(text = "Скачать", color = AppColors.Accent)
            }
        }
    }
}

/** Questions material: unsupported on mobile, show placeholder. */
@Composable
private fun QuestionsCard(
    material: LongreadMaterial,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
            .padding(16.dp),
    ) {
        Text(
            text = material.contentName ?: "Вопросы",
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Этот тип материала недоступен в мобильном приложении",
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Strips HTML tags from a string, returning plain text.
 *
 * Basic implementation for Phase 7. Richer rendering
 * will be evaluated in Phase 11.
 */
internal fun stripHtmlTags(html: String): String =
    html
        .replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
