package io.github.kroune.cumobile.presentation.courses.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.baseline.BaselineTestTags
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.presentation.common.isError
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTabRow
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.EmptyContent
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.SegmentedControl
import io.github.kroune.cumobile.presentation.common.ui.courseCategoryColor
import io.github.kroune.cumobile.presentation.common.ui.courseCategoryLabel
import io.github.kroune.cumobile.presentation.common.ui.stripEmojiPrefix
import io.github.kroune.cumobile.presentation.courses.CoursesComponent
import io.github.kroune.cumobile.presentation.courses.activeCourses
import io.github.kroune.cumobile.presentation.courses.archivedCourses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Courses tab screen with 3 segments: Courses, Grade Sheet, Record Book.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    component: CoursesComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is CoursesComponent.Effect.ShowError -> {
                    actionError = effect.message
                }
            }
        }
    }

    CoursesScreenContent(
        state = state,
        actionError = actionError,
        onIntent = component::onIntent,
        onDismissError = { actionError = null },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoursesScreenContent(
    state: CoursesComponent.State,
    actionError: String? = null,
    onIntent: (CoursesComponent.Intent) -> Unit,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isContentLoading,
        onRefresh = { onIntent(CoursesComponent.Intent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            val pagerState = rememberPagerState(initialPage = state.segment) { 3 }

            LaunchedEffect(state.segment) {
                if (pagerState.currentPage != state.segment) {
                    pagerState.animateScrollToPage(state.segment)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppTabRow(
                currentPage = pagerState.currentPage,
                labels = listOf("курсы", "ведомость", "зачетка"),
                onPageSelected = { page ->
                    onIntent(CoursesComponent.Intent.SelectSegment(page))
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionErrorBar(error = actionError, onDismiss = onDismissError)

            when {
                state.courses.isError -> ErrorContent(
                    error = "Не удалось загрузить курсы",
                    onRetry = { onIntent(CoursesComponent.Intent.Refresh) },
                )
                state.isContentLoading -> CoursesScreenSkeleton()
                else -> HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                ) { page ->
                    when (page) {
                        0 -> CoursesListContent(state = state, onIntent = onIntent)
                        1 -> GradeSheetContent(state = state, onIntent = onIntent)
                        2 -> GradebookContent(state = state, onIntent = onIntent)
                    }
                }
            }
        }
    }
}

// region Segment 0: Courses list with drag-and-drop reordering

@Composable
private fun CoursesListContent(
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = activeCourses(state.courseList, state.courseOrder)
    val archived = archivedCourses(state.courseList, state.courseOrder)

    if (active.isEmpty() && archived.isEmpty()) {
        EmptyContent(text = "Нет курсов")
        return
    }

    var localActive by remember(active) { mutableStateOf(active) }
    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        activeCoursesSection(
            active = active,
            localActive = localActive,
            state = state,
            drag = DragCallbacks(
                draggedId = draggedId,
                dragOffsetY = dragOffsetY,
                onDragStart = { id ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    draggedId = id
                    dragOffsetY = 0f
                },
                onDrag = { dy ->
                    dragOffsetY += dy
                    handleDragSwap(
                        dragOffsetY,
                        draggedId,
                        listState,
                        localActive,
                        scope,
                    ) { items, adj ->
                        localActive = items
                        dragOffsetY += adj
                    }
                },
                onDragEnd = {
                    onIntent(CoursesComponent.Intent.ReorderCourses(localActive.map { it.id }))
                    scope.launch {
                        animate(dragOffsetY, 0f, animationSpec = tween(DropAnimDuration)) { v, _ ->
                            dragOffsetY = v
                        }
                        draggedId = null
                    }
                },
            ),
            onIntent = onIntent,
        )

        archivedCoursesSection(archived, state, onIntent)
    }
}

/**
 * Bundles drag-reorder state and callbacks to keep function parameter count manageable.
 */
private data class DragCallbacks(
    val draggedId: String?,
    val dragOffsetY: Float,
    val onDragStart: (String) -> Unit,
    val onDrag: (Float) -> Unit,
    val onDragEnd: () -> Unit,
)

private fun LazyListScope.activeCoursesSection(
    active: List<Course>,
    localActive: List<Course>,
    state: CoursesComponent.State,
    drag: DragCallbacks,
    onIntent: (CoursesComponent.Intent) -> Unit,
) {
    if (active.isNotEmpty()) {
        item(key = "active_header") {
            SectionHeader(
                title = "Активные",
                count = active.size,
                expanded = state.showActive,
                onClick = { onIntent(CoursesComponent.Intent.ToggleActive) },
            )
        }
    }
    if (state.showActive) {
        val firstActiveId = localActive.firstOrNull()?.id
        items(items = localActive, key = { it.id }) { course ->
            val isDragged = course.id == drag.draggedId
            val baseModifier = if (isDragged) Modifier.zIndex(1f) else Modifier.animateItem()
            val itemModifier = if (course.id == firstActiveId) {
                baseModifier.testTag(BaselineTestTags.FIRST_COURSE_CARD)
            } else {
                baseModifier
            }
            DraggableCourseItem(
                course = course,
                dragOffset = if (isDragged) drag.dragOffsetY else null,
                onClick = { onIntent(CoursesComponent.Intent.OpenCourse(course.id)) },
                onDragStart = { drag.onDragStart(course.id) },
                onDrag = drag.onDrag,
                onDragEnd = drag.onDragEnd,
                modifier = itemModifier,
            )
        }
    }
}

private fun LazyListScope.archivedCoursesSection(
    archived: List<Course>,
    state: CoursesComponent.State,
    onIntent: (CoursesComponent.Intent) -> Unit,
) {
    if (archived.isNotEmpty()) {
        item(key = "archived_header") {
            SectionHeader(
                title = "Архив",
                count = archived.size,
                expanded = state.showArchived,
                onClick = { onIntent(CoursesComponent.Intent.ToggleArchived) },
            )
        }
        if (state.showArchived) {
            items(items = archived, key = { it.id }) { course ->
                CourseListTile(
                    course = course,
                    onClick = { onIntent(CoursesComponent.Intent.OpenCourse(course.id)) },
                )
            }
        }
    }
}

/**
 * Checks if the dragged item has moved past another item and returns the new list + offset adjustment.
 * Returns null if no swap is needed.
 */
private fun trySwapDraggedItem(
    dragOffsetY: Float,
    draggedId: String?,
    layoutInfo: LazyListLayoutInfo,
    currentItems: List<Course>,
): Pair<List<Course>, Float>? {
    val currentId = draggedId ?: return null
    val draggedInfo = layoutInfo.visibleItemsInfo
        .firstOrNull { it.key == currentId } ?: return null
    val draggedCenter = draggedInfo.offset + draggedInfo.size / 2f + dragOffsetY
    val fromIdx = currentItems.indexOfFirst { it.id == currentId }
    if (fromIdx < 0) return null

    val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { item ->
        val itemKey = item.key as? String ?: return@firstOrNull false
        if (itemKey == currentId) return@firstOrNull false
        val toIdx = currentItems.indexOfFirst { it.id == itemKey }
        if (toIdx < 0) return@firstOrNull false
        val itemCenter = item.offset + item.size / 2f
        val hysteresis = item.size * HysteresisRatio
        if (fromIdx < toIdx) {
            draggedCenter > itemCenter + hysteresis
        } else {
            draggedCenter < itemCenter - hysteresis
        }
    } ?: return null

    val toIdx = currentItems.indexOfFirst { it.id == targetItem.key as String }
    val newItems = currentItems.toMutableList().apply { add(toIdx, removeAt(fromIdx)) }
    return newItems to (draggedInfo.offset - targetItem.offset).toFloat()
}

/** Performs a swap if needed and pins the viewport. Returns new offset adjustment or 0. */
private fun handleDragSwap(
    dragOffsetY: Float,
    draggedId: String?,
    listState: LazyListState,
    localActive: List<Course>,
    scope: CoroutineScope,
    onSwap: (newItems: List<Course>, offsetAdj: Float) -> Unit,
) {
    val swap = trySwapDraggedItem(dragOffsetY, draggedId, listState.layoutInfo, localActive)
    if (swap != null) {
        val idx = listState.firstVisibleItemIndex
        val off = listState.firstVisibleItemScrollOffset
        onSwap(swap.first, swap.second)
        scope.launch { listState.scrollToItem(idx, off) }
    }
}

/** Duration in ms for the item to animate back to its slot after dropping. */
private const val DropAnimDuration = 150

/** Fraction of item height used as dead zone to prevent swap oscillation at list boundaries. */
private const val HysteresisRatio = 0.15f

/**
 * Active course item with drag handle and long-press-to-reorder support.
 */
@Composable
private fun DraggableCourseItem(
    course: Course,
    dragOffset: Float?,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val isDragging = dragOffset != null
    val catColor = courseCategoryColor(course.category)

    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (isDragging) {
                Modifier.graphicsLayer {
                    translationY = dragOffset
                    shadowElevation = 8f
                    shape = RoundedCornerShape(12.dp)
                    clip = true
                }
            } else {
                Modifier
            },
        ).clip(RoundedCornerShape(12.dp))
        .background(AppTheme.colors.surface)
        .clickable(enabled = !isDragging, onClick = onClick)
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { currentOnDragStart() },
                onDrag = { change, offset ->
                    change.consume()
                    currentOnDrag(offset.y)
                },
                onDragEnd = { currentOnDragEnd() },
                onDragCancel = { currentOnDragEnd() },
            )
        }.padding(end = 12.dp, top = 10.dp, bottom = 10.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = null,
            tint = AppTheme.colors.textSecondary.copy(alpha = 0.3f),
            modifier = Modifier.padding(horizontal = 8.dp).size(20.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stripEmojiPrefix(course.name),
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = courseCategoryLabel(course.category),
                color = catColor,
                fontSize = 12.sp,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Simple course tile for archived courses (no drag support).
 */
@Composable
private fun CourseListTile(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val catColor = courseCategoryColor(course.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stripEmojiPrefix(course.name),
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = courseCategoryLabel(course.category),
                color = catColor,
                fontSize = 12.sp,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppTheme.colors.textSecondary.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = count.toString(),
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Свернуть" else "Развернуть",
            tint = AppTheme.colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

// endregion

// region Skeleton

private const val SkeletonTileCount = 5

/**
 * Skeleton loading state for the Courses screen.
 *
 * Shows shimmer placeholder tiles matching the courses list layout.
 * The [SegmentedControl] is already rendered above the when-block,
 * so only the content area is replaced.
 */
@Composable
internal fun CoursesScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(SkeletonTileCount) {
            CourseListTileSkeleton()
        }
    }
}

// endregion
