@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.kroune.cumobile.presentation.common.ui.gradeColor
import io.github.kroune.cumobile.presentation.performance.ActivitySummary
import kotlinx.collections.immutable.ImmutableList

/**
 * Performance tab content: weighted activity summaries table.
 */
@Composable
internal fun PerformanceTab(
    summaries: ImmutableList<ActivitySummary>,
    totalContribution: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surface)
                .padding(12.dp),
        ) {
            PerformanceHeaderRow()
            summaries.forEach { summary ->
                PerformanceSummaryRow(summary)
            }
            PerformanceTotalRow(totalContribution)
        }
    }
}

@Composable
private fun PerformanceHeaderRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderCell("Активность", Modifier.weight(2f))
        HeaderCell("Кол-во", Modifier.weight(0.8f))
        HeaderCell("Ср. балл", Modifier.weight(0.9f))
        HeaderCell("x", Modifier.weight(0.3f))
        HeaderCell("Вес", Modifier.weight(0.6f))
        HeaderCell("=", Modifier.weight(0.3f))
        HeaderCell("Итого", Modifier.weight(0.8f))
    }
}

@Composable
private fun HeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = AppTheme.colors.textSecondary,
        fontSize = 11.sp,
        modifier = modifier,
    )
}

@Composable
private fun PerformanceSummaryRow(
    summary: ActivitySummary,
    modifier: Modifier = Modifier,
) {
    val avgColor = scoreRatioColor(summary.averageScore / 10.0)
    val contribColor = scoreRatioColor(summary.totalContribution / 10.0)
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            summary.activityName,
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2f),
        )
        Text(
            text = summary.count.toString(),
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.8f),
        )
        Text(
            text = formatScore(summary.averageScore),
            color = avgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.9f),
        )
        Text(
            text = "x",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.3f),
        )
        Text(
            text = formatScore(summary.weight),
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.6f),
        )
        Text(
            text = "=",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.3f),
        )
        Text(
            text = formatScore(summary.totalContribution),
            color = contribColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f),
        )
    }
}

@Composable
private fun PerformanceTotalRow(
    total: Double,
    modifier: Modifier = Modifier,
) {
    val color = gradeColor(total.toInt())
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(AppTheme.colors.background.copy(alpha = 0.5f))
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Итого",
            color = AppTheme.colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(5.3f),
        )
        Text(
            formatScore(total),
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f),
        )
    }
}
