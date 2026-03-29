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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lazyitems.ChildItemsLifecycleController
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import kotlinx.coroutines.launch

/**
 * Main longread screen displaying materials as ChildItems.
 *
 * Each material is its own component with automatic lifecycle management.
 * The LazyColumn renders materials via `item.Render()` — no type dispatch needed.
 */
@OptIn(ExperimentalDecomposeApi::class)
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
                is LongreadComponent.Effect.SnackBarEffect -> {
                    coroutineScope.launch {
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = effect.actionLabel,
                            withDismissAction = effect.withDismissAction,
                            duration = effect.duration,
                        )
                        effect.onSnackbarResult(snackbarResult)
                    }
                }
            }
        }
    }

    LongreadScreenContent(
        state = state,
        component = component,
        actionError = actionError,
        snackbarHostState = snackbarHostState,
        onIntent = component::onIntent,
        onDismissError = { actionError = null },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalDecomposeApi::class)
@Composable
internal fun LongreadScreenContent(
    state: LongreadComponent.State,
    component: LongreadComponent? = null,
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
            onRefresh = { onIntent(LongreadComponent.Intent.Navigation.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                DetailTopBar(
                    title = state.title,
                    onBack = { onIntent(LongreadComponent.Intent.Navigation.Back) },
                    trailingContent = {
                        TextButton(
                            onClick = {
                                onIntent(LongreadComponent.Intent.Search.ToggleSearch)
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
                        onRetry = { onIntent(LongreadComponent.Intent.Navigation.Refresh) },
                    )
                    !state.isLoading && state.materials.isEmpty() -> EmptyMaterialsContent()
                    component != null -> MaterialList(component = component)
                    else -> EmptyMaterialsContent()
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

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun MaterialList(
    component: LongreadComponent,
    modifier: Modifier = Modifier,
) {
    val childItems by component.materialItems.subscribeAsState()
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
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
        items(items = childItems.items, key = { it.id }) { config ->
            val item = remember(config, childItems) {
                component.materialItems[config]
            }
            item.Render()
        }
    }

    ChildItemsLifecycleController(
        items = component.materialItems,
        lazyListState = lazyListState,
        itemIndexConverter = { it },
    )
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
                onIntent(LongreadComponent.Intent.Search.UpdateSearchQuery(it))
            },
            onSearch = { onIntent(LongreadComponent.Intent.Search.NextMatch) },
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
                .clickable { onIntent(LongreadComponent.Intent.Search.ToggleSearch) },
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
                onIntent(LongreadComponent.Intent.Search.PreviousMatch)
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
                onIntent(LongreadComponent.Intent.Search.NextMatch)
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

private const val SkeletonMaterialCount = 3

@Composable
private fun LongreadScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        repeat(SkeletonMaterialCount) {
            LongreadMaterialSkeleton()
        }
    }
}
