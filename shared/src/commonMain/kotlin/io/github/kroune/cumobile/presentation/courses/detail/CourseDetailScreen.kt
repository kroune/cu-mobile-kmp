@file:Suppress("TooManyFunctions", "MagicNumber")

package io.github.kroune.cumobile.presentation.courses.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.CourseTheme
import io.github.kroune.cumobile.data.model.Longread
import io.github.kroune.cumobile.data.model.ThemeExercise
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.formatDeadlineShort
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix

/**
 * Course detail screen showing themes with expandable longreads.
 *
 * Matches the Flutter reference CoursePage layout:
 * - Top bar with course name and search
 * - Numbered theme cards that expand to show longreads
 * - Longreads show exercises with deadlines
 */
@Composable
fun CourseDetailScreen(
    component: CourseDetailComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val overview = state.overview
    val courseName = overview?.name?.let { stripEmojiPrefix(it) }.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        // Top bar
        DetailTopBar(
            title = courseName,
            onBack = onBack,
        )

        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = {
                    component.onIntent(CourseDetailComponent.Intent.Refresh)
                },
            )
            overview != null -> ThemesContent(
                state = state,
                component = component,
            )
        }
    }
}

@Composable
private fun ThemesContent(
    state: CourseDetailComponent.State,
    component: CourseDetailComponent,
    modifier: Modifier = Modifier,
) {
    val themes = state.overview?.themes.orEmpty()
    val filtered = filteredThemes(themes, state.searchQuery)

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search field
        SearchField(
            query = state.searchQuery,
            onQueryChange = {
                component.onIntent(CourseDetailComponent.Intent.Search(it))
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Ничего не найдено",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = filtered,
                    key = { _, theme -> theme.id },
                ) { index, theme ->
                    ThemeCard(
                        theme = theme,
                        index = index + 1,
                        isExpanded = theme.id in state.expandedThemeIds,
                        onToggle = {
                            component.onIntent(
                                CourseDetailComponent.Intent.ToggleTheme(theme.id),
                            )
                        },
                        onOpenLongread = { longreadId, themeId ->
                            component.onIntent(
                                CourseDetailComponent.Intent.OpenLongread(
                                    longreadId = longreadId,
                                    courseId = state.courseId,
                                    themeId = themeId,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Поиск по темам...",
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.Accent,
            focusedIndicatorColor = AppColors.Accent,
            unfocusedIndicatorColor = AppColors.TextSecondary.copy(alpha = 0.3f),
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Expandable theme card with numbered header.
 */
@Composable
private fun ThemeCard(
    theme: CourseTheme,
    index: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenLongread: (longreadId: Int, themeId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface),
    ) {
        // Header
        ThemeHeader(
            theme = theme,
            index = index,
            isExpanded = isExpanded,
            onClick = onToggle,
        )

        // Expanded content: longreads list
        if (isExpanded) {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                theme.longreads.forEach { longread ->
                    LongreadRow(
                        longread = longread,
                        onClick = { onOpenLongread(longread.id, theme.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeHeader(
    theme: CourseTheme,
    index: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Theme number badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.Accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                color = AppColors.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.name,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (theme.hasExercises) {
                Text(
                    text = exerciseCountLabel(theme.totalExercises),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Text(
            text = if (isExpanded) "\u25B2" else "\u25BC",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

/**
 * Single longread row within an expanded theme.
 */
@Composable
private fun LongreadRow(
    longread: Longread,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF252525))
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon based on whether longread has exercises
            Text(
                text = if (longread.exercises.isNotEmpty()) {
                    "\uD83D\uDCDD"
                } else {
                    "\uD83D\uDCC4"
                },
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = longread.name,
                color = AppColors.TextPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "\u203A",
                color = AppColors.TextSecondary,
                fontSize = 18.sp,
            )
        }

        // Exercises within the longread
        if (longread.exercises.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            longread.exercises.forEach { exercise ->
                ExerciseRow(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: ThemeExercise,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = exercise.name,
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (exercise.deadline != null) {
            Text(
                text = formatDeadlineShort(exercise.deadline),
                color = AppColors.TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

// region Helper functions

/**
 * Returns Russian plural form for exercise count.
 * E.g.: "1 задание", "2 задания", "5 заданий".
 */
private fun exerciseCountLabel(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val form = when {
        mod100 in 11..19 -> "заданий"
        mod10 == 1 -> "задание"
        mod10 in 2..4 -> "задания"
        else -> "заданий"
    }
    return "$count $form"
}

// endregion
