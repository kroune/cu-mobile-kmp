package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.plus

/**
 * Week picker with navigation arrows and day selection pills.
 */
@Composable
internal fun WeekPicker(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        WeekNavigationHeader(
            weekStart = weekStart,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
        )

        Spacer(modifier = Modifier.height(6.dp))

        WeekDaysRow(
            weekStart = weekStart,
            selectedDate = selectedDate,
            onDaySelected = onDateSelected,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun WeekNavigationHeader(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Предыдущая неделя",
                tint = AppTheme.colors.accent,
            )
        }

        Text(
            text = formatWeekRange(weekStart),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.textSecondary,
        )

        IconButton(onClick = onNextWeek) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Следующая неделя",
                tint = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun WeekDaysRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
) {
    val weekDays = generateWeekDays(weekStart)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        weekDays.forEach { dayData ->
            DayPill(
                dayName = dayData.dayName,
                date = dayData.dateString,
                isSelected = dayData.date == selectedDate,
                onClick = { onDaySelected(dayData.date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayPill(
    dayName: String,
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        if (isSelected) AppTheme.colors.accent else AppTheme.colors.surface,
    )
    val textColor = if (isSelected) AppTheme.colors.background else AppTheme.colors.textPrimary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = dayName,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
        Text(
            text = date,
            fontSize = 13.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

/**
 * Schedule card with time badge on the right side.
 */
@Composable
internal fun ScheduleCard(
    classData: ClassData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppTheme.colors.surface)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = classData.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (classData.room.isNotEmpty()) {
                    Text(
                        text = classData.room,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = AppTheme.colors.accent,
                    )
                }
                if (classData.type.isNotEmpty()) {
                    Text(
                        text = classData.type,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.background.copy(alpha = 0.8f))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = classData.startTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.accent,
                lineHeight = 14.sp,
            )
            Text(
                text = classData.endTime,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════════
// WEEK FORMATTING
// ════════════════════════════════════════════════════════════════════════════════════

private val russianMonthNames = MonthNames(
    "января",
    "февраля",
    "марта",
    "апреля",
    "мая",
    "июня",
    "июля",
    "августа",
    "сентября",
    "октября",
    "ноября",
    "декабря",
)

private val dayAndMonthFormat = LocalDate.Format {
    day(Padding.NONE)
    char(' ')
    monthName(russianMonthNames)
}

private fun formatWeekRange(weekStart: LocalDate): String {
    val endDate = weekStart + DatePeriod(days = 6)
    val startDay = weekStart.day
    val endDay = endDate.day

    val endFormatted = dayAndMonthFormat.format(endDate)
    val monthName = endFormatted.substringAfter(' ')

    return if (weekStart.month == endDate.month) {
        "$startDay - $endDay $monthName"
    } else {
        val startFormatted = dayAndMonthFormat.format(weekStart)
        "$startFormatted - $endFormatted"
    }
}

private data class DayData(
    val dayName: String,
    val dateString: String,
    val date: LocalDate,
)

private val DayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

private fun generateWeekDays(weekStart: LocalDate): List<DayData> =
    DayNames.indices.map { index ->
        val currentDate = weekStart.plus(DatePeriod(days = index))
        DayData(
            dayName = DayNames[index],
            dateString = currentDate.day.toString(),
            date = currentDate,
        )
    }

@Preview
@Composable
private fun PreviewHomeWithScheduleDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(
                state = previewHomeWithScheduleState,
                onIntent = {},
                onTaskClick = {},
                onCourseClick = {},
            )
        }
    }
}
