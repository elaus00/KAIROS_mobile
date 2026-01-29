package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * MonthView 컴포넌트 (PRD v4.0)
 * 월간 달력 뷰 (접기/펼치기 지원)
 *
 * @param selectedDate 선택된 날짜
 * @param datesWithSchedules 일정이 있는 날짜 목록
 * @param isExpanded 펼침 상태
 * @param onDateSelected 날짜 선택 콜백
 * @param onToggleExpand 펼침 토글 콜백
 * @param onMonthChange 월 변경 콜백
 */
@Composable
fun MonthView(
    selectedDate: LocalDate,
    datesWithSchedules: Set<LocalDate> = emptySet(),
    isExpanded: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onToggleExpand: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(selectedDate)

    Column(modifier = modifier.fillMaxWidth()) {
        // 월/년 헤더 + 토글 버튼
        MonthHeader(
            yearMonth = currentMonth,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onPreviousMonth = { onMonthChange(currentMonth.minusMonths(1)) },
            onNextMonth = { onMonthChange(currentMonth.plusMonths(1)) }
        )

        // 펼침 상태일 때만 월간 캘린더 표시
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200))
        ) {
            Column {
                // 요일 헤더
                DayOfWeekHeader()

                // 날짜 그리드
                MonthGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    today = today,
                    datesWithSchedules = datesWithSchedules,
                    onDateSelected = onDateSelected
                )
            }
        }

        // 접힌 상태일 때 주간 뷰
        AnimatedVisibility(
            visible = !isExpanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200))
        ) {
            WeekPicker(
                selectedDate = selectedDate,
                datesWithSchedules = datesWithSchedules,
                onDateSelected = onDateSelected
            )
        }
    }
}

/**
 * 월 헤더 (년월 표시 + 이전/다음 버튼 + 펼침 토글)
 */
@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜 + 토글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleExpand() }
                .padding(8.dp)
        ) {
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${yearMonth.year}년",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "접기" else "펼치기",
                tint = colors.icon,
                modifier = Modifier.size(20.dp)
            )
        }

        // 이전/다음 버튼
        Row {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = colors.icon
                )
            }
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "다음 달",
                    tint = colors.icon
                )
            }
        }
    }
}

/**
 * 요일 헤더 (일~토)
 */
@Composable
private fun DayOfWeekHeader(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEachIndexed { index, day ->
            val textColor = when (index) {
                0 -> colors.danger // 일요일
                6 -> colors.textSecondary // 토요일
                else -> colors.textMuted
            }
            Text(
                text = day,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 월간 날짜 그리드
 */
@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    datesWithSchedules: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 월의 첫날과 마지막날
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // 첫 주 시작 요일 (일요일 기준)
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)

    // 전체 셀 수 계산
    val totalDays = lastDayOfMonth.dayOfMonth
    val totalCells = startDayOfWeek + totalDays
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        for (week in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val dayIndex = week * 7 + dayOfWeek - startDayOfWeek + 1
                    if (dayIndex in 1..totalDays) {
                        val date = yearMonth.atDay(dayIndex)
                        MonthDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            hasSchedule = datesWithSchedules.contains(date),
                            isCurrentMonth = true,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // 빈 셀
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

/**
 * 개별 날짜 셀
 */
@Composable
private fun MonthDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 150),
        label = "bgColor"
    )

    val textColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        !isCurrentMonth -> colors.textMuted.copy(alpha = 0.4f)
        isToday -> colors.accent
        date.dayOfWeek == DayOfWeek.SUNDAY -> colors.danger
        else -> colors.text
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
            )

            // 일정 dot
            if (hasSchedule) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                if (colors.isDark) colors.background else Color.White
                            } else colors.accent
                        )
                )
            }
        }
    }
}
