package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/**
 * CalendarCard 컴포넌트
 * 날짜 헤더 + 주간/월간 뷰를 카드로 감싼 형태
 * 아래로 스와이프 → 월간 확장, 위로 스와이프 → 주간 축소
 * 월간에서 좌우 스와이프 → 달 변경
 */
@Composable
fun CalendarCard(
    selectedDate: LocalDate,
    datesWithSchedules: Set<LocalDate> = emptySet(),
    isExpanded: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onToggleExpand: () -> Unit,
    onMonthChange: (YearMonth) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(selectedDate)

    // 스와이프 제스처 감지용 누적값
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .pointerInput(isExpanded, currentMonth) {
                detectDragGestures(
                    onDragStart = {
                        dragAmountX = 0f
                        dragAmountY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountX += dragAmount.x
                        dragAmountY += dragAmount.y
                    },
                    onDragEnd = {
                        val threshold = 50f
                        if (abs(dragAmountY) > abs(dragAmountX)) {
                            // 수직 스와이프 우선
                            if (dragAmountY > threshold && !isExpanded) {
                                // 아래로 스와이프 → 월간 확장
                                onToggleExpand()
                            } else if (dragAmountY < -threshold && isExpanded) {
                                // 위로 스와이프 → 주간 축소
                                onToggleExpand()
                            }
                        } else if (isExpanded && abs(dragAmountX) > threshold) {
                            // 월간 뷰에서 좌우 스와이프 → 달 변경
                            if (dragAmountX < -threshold) {
                                // 왼쪽 스와이프 → 다음 달
                                onMonthChange(currentMonth.plusMonths(1))
                            } else if (dragAmountX > threshold) {
                                // 오른쪽 스와이프 → 이전 달
                                onMonthChange(currentMonth.minusMonths(1))
                            }
                        }
                    }
                )
            }
            .padding(16.dp)
    ) {
        // 월 헤더 + 좌우 이동 버튼 + 토글 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이전 달 버튼 — 리플 복원
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "이전 달",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onMonthChange(currentMonth.minusMonths(1)) }
            )

            // 월 텍스트 (클릭 시 펼침/접기)
            Text(
                text = "${currentMonth.monthValue}월",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleExpand
                )
            )

            // 다음 달 버튼 — 리플 복원
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "다음 달",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onMonthChange(currentMonth.plusMonths(1)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 주간/월간 뷰 전환 애니메이션
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(200)) using
                    SizeTransform(clip = false) { _, _ ->
                        tween(350)
                    }
            },
            label = "calendarExpand"
        ) { expanded ->
            if (expanded) {
                // 월간 뷰 (펼침 상태)
                Column {
                    // 요일 헤더
                    CalendarDayHeader()

                    Spacer(modifier = Modifier.height(4.dp))

                    // 월간 그리드
                    CalendarMonthGrid(
                        yearMonth = currentMonth,
                        selectedDate = selectedDate,
                        today = today,
                        datesWithSchedules = datesWithSchedules,
                        onDateSelected = onDateSelected
                    )
                }
            } else {
                // 주간 뷰 (기본 상태)
                CalendarWeekRow(
                    selectedDate = selectedDate,
                    today = today,
                    datesWithSchedules = datesWithSchedules,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

/**
 * 주간 달력 Row (일~토)
 */
@Composable
private fun CalendarWeekRow(
    selectedDate: LocalDate,
    today: LocalDate,
    datesWithSchedules: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 선택된 날짜가 포함된 주의 시작일 (일요일)
    val startOfWeek = selectedDate.minusDays(
        selectedDate.dayOfWeek.value.toLong() % 7
    )
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDates.forEachIndexed { index, date ->
            CalendarDayCell(
                date = date,
                dayName = dayNames[index],
                isSelected = date == selectedDate,
                isToday = date == today,
                hasSchedule = datesWithSchedules.contains(date) && date != selectedDate,
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 개별 날짜 셀 (주간 뷰용)
 */
@Composable
private fun CalendarDayCell(
    date: LocalDate,
    dayName: String,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "bgColor"
    )

    val textColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        else -> colors.text
    }

    // 요일은 선택 여부와 관계없이 항상 textMuted
    val dayNameColor = colors.textMuted

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요일
        Text(
            text = dayName,
            color = dayNameColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 날짜 (원형 배경)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 일정 dot (선택되지 않았고 일정 있을 때만)
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (hasSchedule) colors.textSecondary else Color.Transparent
                )
        )
    }
}

/**
 * 요일 헤더 (월간 뷰용)
 */
@Composable
private fun CalendarDayHeader(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEach { day ->
            Text(
                text = day,
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 월간 그리드
 */
@Composable
private fun CalendarMonthGrid(
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

    // 첫 주 시작 요일 (일요일 = 0)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    // 전체 셀 수 계산
    val totalDays = lastDayOfMonth.dayOfMonth
    val totalCells = startDayOfWeek + totalDays
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier) {
        for (week in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val dayIndex = week * 7 + dayOfWeek - startDayOfWeek + 1
                    if (dayIndex in 1..totalDays) {
                        val date = yearMonth.atDay(dayIndex)
                        val isSelected = date == selectedDate
                        val hasSchedule = datesWithSchedules.contains(date) && !isSelected

                        MonthDayCell(
                            date = date,
                            isSelected = isSelected,
                            isToday = date == today,
                            hasSchedule = hasSchedule,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // 빈 셀
                        Spacer(modifier = Modifier.weight(1f).padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

/**
 * 개별 날짜 셀 (월간 뷰용)
 */
@Composable
private fun MonthDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "bgColor"
    )

    val textColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        isToday -> colors.accent
        else -> colors.text
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 날짜 (원형 배경)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal
            )
        }

        // 일정 dot
        if (hasSchedule) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(colors.textSecondary)
            )
        }
    }
}
