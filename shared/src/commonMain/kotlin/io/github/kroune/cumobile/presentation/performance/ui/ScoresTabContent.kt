package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.performance.ExerciseWithScore
import kotlinx.collections.immutable.ImmutableList

/**
 * Scores tab content: activity filter chips + exercise tiles.
 */
@Composable
internal fun ScoresTab(
    exercises: ImmutableList<ExerciseWithScore>,
    activityNames: ImmutableList<String>,
    activeFilter: String?,
    onFilterActivity: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (activityNames.size > 1) {
            ActivityFilterChips(
                activityNames = activityNames,
                activeFilter = activeFilter,
                onFilterActivity = onFilterActivity,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            items(exercises, key = { it.exercise.id }) { item ->
                ExerciseTile(item)
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ActivityFilterChips(
    activityNames: ImmutableList<String>,
    activeFilter: String?,
    onFilterActivity: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = activeFilter == null,
            onClick = { onFilterActivity(null) },
            label = { Text("Все активности") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = AppTheme.colors.surface,
                labelColor = AppTheme.colors.textSecondary,
                selectedContainerColor = AppTheme.colors.accent.copy(alpha = 0.2f),
                selectedLabelColor = AppTheme.colors.accent,
            ),
        )
        activityNames.forEach { name ->
            FilterChip(
                selected = activeFilter == name,
                onClick = { onFilterActivity(name) },
                label = { Text(name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppTheme.colors.surface,
                    labelColor = AppTheme.colors.textSecondary,
                    selectedContainerColor = AppTheme.colors.accent.copy(alpha = 0.2f),
                    selectedLabelColor = AppTheme.colors.accent,
                ),
            )
        }
    }
}

@Composable
private fun ExerciseTile(
    item: ExerciseWithScore,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.themeName,
                color = AppTheme.colors.textSecondary,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            ScoreBadge(score = item.scoreValue, maxScore = item.maxScore)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.exercise.name,
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.activityName,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ScoreBadge(
    score: Double,
    maxScore: Int,
    modifier: Modifier = Modifier,
) {
    val ratio = if (maxScore > 0) score / maxScore else 0.0
    val color = scoreRatioColor(ratio)
    val text = formatScore(score) + " / $maxScore"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
