@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.formatSizeBytes
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import kotlinx.collections.immutable.persistentListOf
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.AudioMaterialCard
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.HtmlContent
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.ImageMaterialCard
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.VideoMaterialCard
import kotlinx.coroutines.launch

/**
 * Main longread screen displaying materials within a longread.
 *
 * Renders different card types based on material discriminator:
 * - markdown: HTML content (stripped to plain text)
 * - file: download card with filename and size
 * - coding: task management card (delegated to [CodingMaterialCard])
 * - questions: unsupported placeholder
 */
@Composable
fun LongreadScreen(
    component: LongreadComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is LongreadComponent.Effect.ShowError -> {
                    actionError = effect.message
                }
                is LongreadComponent.Effect.ShowSuccess -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = "Показать",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            component.onIntent(
                                LongreadComponent.Intent.NavigateToFiles,
                            )
                        }
                    }
                }
            }
        }
    }

    LongreadScreenContent(
        state = state,
        actionError = actionError,
        snackbarHostState = snackbarHostState,
        onIntent = component::onIntent,
        onDismissError = { actionError = null },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LongreadScreenContent(
    state: LongreadComponent.State,
    actionError: String?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (LongreadComponent.Intent) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onIntent(LongreadComponent.Intent.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                DetailTopBar(
                    title = state.title,
                    onBack = { onIntent(LongreadComponent.Intent.Back) },
                    trailingContent = {
                        TextButton(
                            onClick = {
                                onIntent(LongreadComponent.Intent.ToggleSearch)
                            },
                        ) {
                            Text(
                                text = "\uD83D\uDD0D",
                                fontSize = 18.sp,
                            )
                        }
                    },
                )

                ActionErrorBar(error = actionError, onDismiss = onDismissError)

                if (state.isSearchVisible) {
                    SearchBar(
                        state = state,
                        onIntent = onIntent,
                    )
                }

                when {
                    state.isLoading && state.materials.isEmpty() ->
                        LongreadScreenSkeleton()
                    state.error != null && state.materials.isEmpty() -> ErrorContent(
                        error = state.error,
                        onRetry = { onIntent(LongreadComponent.Intent.Refresh) },
                    )
                    !state.isLoading && state.materials.isEmpty() -> EmptyMaterialsContent()
                    else -> MaterialList(
                        state = state,
                        onIntent = onIntent,
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppTheme.colors.surface,
                contentColor = AppTheme.colors.textPrimary,
                actionColor = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun EmptyMaterialsContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Материалы пока не добавлены",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun MaterialList(
    state: LongreadComponent.State,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 12.dp,
            bottom = 12.dp + WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding(),
        ),
    ) {
        items(state.materials, key = { it.id }) { material ->
            when {
                material.isMarkdown -> MarkdownCard(
                    material = material,
                    searchQuery = state.searchQuery,
                )
                material.isFile -> FileCard(material, onIntent)
                material.isCoding -> CodingMaterialCard(
                    material = material,
                    state = state,
                    onIntent = onIntent,
                )
                material.isQuestions -> QuestionsCard(material)
                material.isImage -> ImageMaterialCard(material = material)
                material.isVideo || material.isVideoPlatform ->
                    VideoMaterialCard(material = material)
                material.isAudio -> AudioMaterialCard(material = material)
                else -> MarkdownCard(
                    material = material,
                    searchQuery = state.searchQuery,
                )
            }
        }
    }
}

/** Search bar with query input, match counter, and prev/next navigation. */
@Composable
private fun SearchBar(
    state: LongreadComponent.State,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SearchInput(
            query = state.searchQuery,
            onQueryChange = {
                onIntent(LongreadComponent.Intent.UpdateSearchQuery(it))
            },
            onSearch = { onIntent(LongreadComponent.Intent.NextMatch) },
            modifier = Modifier.weight(1f),
        )
        if (state.searchQuery.isNotEmpty()) {
            SearchNavigation(
                matchCount = state.searchMatchCount,
                currentIndex = state.currentMatchIndex,
                onIntent = onIntent,
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onIntent(LongreadComponent.Intent.ToggleSearch) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\u2715",
                fontSize = 16.sp,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
        ),
        cursorBrush = SolidColor(AppTheme.colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            if (query.isEmpty()) {
                Text(
                    text = "Поиск...",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 14.sp,
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun SearchNavigation(
    matchCount: Int,
    currentIndex: Int,
    onIntent: (LongreadComponent.Intent) -> Unit,
) {
    val hasMatches = matchCount > 0
    val enabledColor = AppTheme.colors.accent
    val disabledColor = AppTheme.colors.textSecondary
    Text(
        text = if (hasMatches) "${currentIndex + 1}/$matchCount" else "0/0",
        color = AppTheme.colors.textSecondary,
        fontSize = 12.sp,
    )
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = hasMatches) {
                onIntent(LongreadComponent.Intent.PreviousMatch)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u25B2",
            fontSize = 14.sp,
            color = if (hasMatches) enabledColor else disabledColor,
        )
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = hasMatches) {
                onIntent(LongreadComponent.Intent.NextMatch)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u25BC",
            fontSize = 14.sp,
            color = if (hasMatches) enabledColor else disabledColor,
        )
    }
}

/** Markdown material: renders HTML content as native Compose UI. */
@Composable
private fun MarkdownCard(
    material: LongreadMaterial,
    searchQuery: String = "",
    modifier: Modifier = Modifier,
) {
    val html = material.viewContent.orEmpty()
    val title = material.contentName
    val blocks = remember(html) {
        if (html.isBlank()) persistentListOf() else parseHtmlToBlocks(html)
    }
    if (title.isNullOrBlank() && blocks.isEmpty()) return

    Column(
        modifier = modifier
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
                searchQuery = searchQuery,
            )
        }
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
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "\uD83D\uDCC4 $filename",
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            material.length?.let { size ->
                Text(
                    text = formatSizeBytes(size.toLong()),
                    color = AppTheme.colors.textSecondary,
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
                Text(text = "Скачать", color = AppTheme.colors.accent)
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
            .background(AppTheme.colors.surface)
            .padding(16.dp),
    ) {
        Text(
            text = material.contentName ?: "Вопросы",
            color = AppTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Этот тип материала недоступен в мобильном приложении",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private const val SkeletonMaterialCount = 3
private val SkeletonMaterialSpacing = 12.dp
private val SkeletonMaterialPadding = 16.dp
private val SkeletonMaterialTopSpacing = 12.dp

/**
 * Skeleton loading state for the Longread screen.
 *
 * Shows shimmer placeholder blocks matching the material list layout.
 * The [DetailTopBar] is already rendered above the when-block.
 */
@Composable
private fun LongreadScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SkeletonMaterialPadding),
        verticalArrangement = Arrangement.spacedBy(SkeletonMaterialSpacing),
    ) {
        Spacer(Modifier.height(SkeletonMaterialTopSpacing))
        repeat(SkeletonMaterialCount) {
            LongreadMaterialSkeleton()
        }
    }
}
