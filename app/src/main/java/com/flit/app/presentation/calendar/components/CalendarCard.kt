package com.flit.app.presentation.calendar.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.ui.theme.FlitTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * CalendarCard 컴포넌트
 * 날짜 헤더 + 주간/월간 뷰를 카드로 감싼 형태
 * 아래로 스와이프 → 월간 확장, 위로 스와이프 → 주간 축소
 * 월간에서 좌우 스와이프 → 달 변경
 *
 * selectedDate가 null이면 선택된 날짜 없음 (다른 달 탐색 시)
 * 오늘 날짜는 항상 "오늘" 라벨로 표시됨
 */
@Composable
fun CalendarCard(
    selectedDate: LocalDate?,
    currentMonth: YearMonth,
    datesWithSchedules: Set<LocalDate> = emptySet(),
    isExpanded: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onToggleExpand: () -> Unit,
    onMonthChange: (YearMonth) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val today = LocalDate.now()
    val scope = rememberCoroutineScope()

    // 주간 뷰에서 표시할 기준 날짜 (주차 결정용)
    val weekReferenceDate = selectedDate
        ?: if (YearMonth.from(today) == currentMonth) today
        else currentMonth.atDay(1)

    // pointerInput 내부에서 최신 값을 참조하기 위한 State
    val currentWeekRef by rememberUpdatedState(weekReferenceDate)
    val currentSelectedDate by rememberUpdatedState(selectedDate)

    // 스와이프 제스처 감지용 누적값
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    var weekDragOffsetX by remember { mutableFloatStateOf(0f) }
    var weekContainerWidthPx by remember { mutableFloatStateOf(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .pointerInput(isExpanded, currentMonth) {
                detectDragGestures(
                    onDragStart = {
                        settleJob?.cancel()
                        settleJob = null
                        dragAmountX = 0f
                        dragAmountY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountX += dragAmount.x
                        dragAmountY += dragAmount.y
                        if (!isExpanded && abs(dragAmountX) > abs(dragAmountY)) {
                            val width = weekContainerWidthPx.takeIf { it > 0f } ?: 1f
                            weekDragOffsetX = (weekDragOffsetX + dragAmount.x)
                                .coerceIn(-width, width)
                        }
                    },
                    onDragEnd = {
                        val threshold = 24f
                        if (abs(dragAmountY) > abs(dragAmountX)) {
                            // 수직 스와이프 우선
                            if (dragAmountY > threshold && !isExpanded) {
                                // 아래로 스와이프 → 월간 확장
                                onToggleExpand()
                            } else if (dragAmountY < -threshold && isExpanded) {
                                // 위로 스와이프 → 주간 축소
                                onToggleExpand()
                            }
                        } else if (abs(dragAmountX) > threshold) {
                            if (isExpanded) {
                                // 월간 뷰에서 좌우 스와이프 → 달 변경
                                if (dragAmountX < -threshold) {
                                    // 왼쪽 스와이프 → 다음 달
                                    onMonthChange(currentMonth.plusMonths(1))
                                } else if (dragAmountX > threshold) {
                                    // 오른쪽 스와이프 → 이전 달
                                    onMonthChange(currentMonth.minusMonths(1))
                                }
                            } else {
                                // 주간 뷰에서 좌우 스와이프 → 인터랙티브 주 이동
                                val width = weekContainerWidthPx.takeIf { it > 0f } ?: 1f
                                val commitThreshold = 18f
                                val deltaWeek = when {
                                    weekDragOffsetX <= -commitThreshold -> 1L
                                    weekDragOffsetX >= commitThreshold -> -1L
                                    else -> 0L
                                }

                                settleJob = scope.launch {
                                    if (deltaWeek != 0L) {
                                        val target = if (deltaWeek > 0) -width else width
                                        animate(
                                            initialValue = weekDragOffsetX,
                                            targetValue = target,
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) { value, _ -> weekDragOffsetX = value }
                                        weekDragOffsetX = 0f
                                        val ref = currentSelectedDate ?: currentWeekRef
                                        onDateSelected(ref.plusWeeks(deltaWeek))
                                    } else {
                                        animate(
                                            initialValue = weekDragOffsetX,
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = 320,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) { value, _ -> weekDragOffsetX = value }
                                    }
                                }
                            }
                        } else if (!isExpanded) {
                            settleJob = scope.launch {
                                animate(
                                    initialValue = weekDragOffsetX,
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = 320,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { value, _ -> weekDragOffsetX = value }
                            }
                        }
                    }
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
    ) {
        // 월 헤더
        val weekOfMonth = ((weekReferenceDate.dayOfMonth - 1) / 7) + 1
        var showMonthDropdown by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 월 텍스트 (클릭 시 월 선택 팝업)
            Box {
                Text(
                    text = if (isExpanded) "${currentMonth.monthValue}월" else "${currentMonth.monthValue}월 ${weekOfMonth}주차",
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showMonthDropdown = !showMonthDropdown }
                    )
                )

                // 월 선택 그리드 팝업
                if (showMonthDropdown) {
                    Popup(
                        alignment = Alignment.TopStart,
                        onDismissRequest = { showMonthDropdown = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.card)
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            for (row in 0..2) {
                                Row {
                                    for (col in 0..3) {
                                        val month = row * 4 + col + 1
                                        val isCurrentMonth = month == currentMonth.monthValue
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isCurrentMonth) colors.accent
                                                    else Color.Transparent
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    showMonthDropdown = false
                                                    onMonthChange(YearMonth.of(currentMonth.year, month))
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${month}월",
                                                color = if (isCurrentMonth) {
                                                    if (colors.isDark) colors.background else Color.White
                                                } else colors.text,
                                                fontSize = 13.sp,
                                                fontWeight = if (isCurrentMonth) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 오른쪽: 펼치기/접기 아이콘 (항상 표시)
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 주간/월간 뷰 전환 애니메이션
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                    fadeOut(animationSpec = tween(200)) using
                    SizeTransform(clip = false) { _, _ ->
                        tween(200)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .onSizeChanged { size ->
                            weekContainerWidthPx = size.width.toFloat()
                        }
                ) {
                    val widthPx = weekContainerWidthPx.takeIf { it > 0f } ?: 0f
                    val roundedOffset = weekDragOffsetX.roundToInt()
                    val prevRefDate = weekReferenceDate.minusWeeks(1)
                    val nextRefDate = weekReferenceDate.plusWeeks(1)

                    if (widthPx <= 0f) {
                        // 폭 측정 전에는 단일 주차만 렌더링
                        CalendarWeekRow(
                            referenceDate = weekReferenceDate,
                            selectedDate = selectedDate,
                            today = today,
                            datesWithSchedules = datesWithSchedules,
                            onDateSelected = onDateSelected
                        )
                    } else {
                        CalendarWeekRow(
                            referenceDate = prevRefDate,
                            selectedDate = selectedDate,
                            today = today,
                            datesWithSchedules = datesWithSchedules,
                            onDateSelected = onDateSelected,
                            modifier = Modifier.offset {
                                IntOffset((-widthPx).roundToInt() + roundedOffset, 0)
                            }
                        )

                        CalendarWeekRow(
                            referenceDate = weekReferenceDate,
                            selectedDate = selectedDate,
                            today = today,
                            datesWithSchedules = datesWithSchedules,
                            onDateSelected = onDateSelected,
                            modifier = Modifier.offset { IntOffset(roundedOffset, 0) }
                        )

                        CalendarWeekRow(
                            referenceDate = nextRefDate,
                            selectedDate = selectedDate,
                            today = today,
                            datesWithSchedules = datesWithSchedules,
                            onDateSelected = onDateSelected,
                            modifier = Modifier.offset {
                                IntOffset(widthPx.roundToInt() + roundedOffset, 0)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 주간 달력 Row (일~토)
 * referenceDate: 주차 결정용 기준 날짜
 * selectedDate: 사용자가 선택한 날짜 (null = 선택 없음)
 */
@Composable
private fun CalendarWeekRow(
    referenceDate: LocalDate,
    selectedDate: LocalDate?,
    today: LocalDate,
    datesWithSchedules: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 기준 날짜가 포함된 주의 시작일 (일요일)
    val startOfWeek = referenceDate.minusDays(
        referenceDate.dayOfWeek.value.toLong() % 7
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
                hasSchedule = datesWithSchedules.contains(date),
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 개별 날짜 셀 (주간 뷰용)
 * - 선택된 날짜: accent 배경
 * - 오늘: accent 텍스트 + "오늘" 라벨 (항상 표시)
 * - 일정 dot: 오늘이 아니고 일정이 있을 때
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
    val colors = FlitTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val textColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        isToday -> colors.accent
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

        Spacer(modifier = Modifier.height(2.dp))

        // 하단 영역: 오늘 라벨 또는 일정 dot
        Box(
            modifier = Modifier.height(14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isToday) {
                Text(
                    text = "오늘",
                    color = colors.accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (hasSchedule && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.textSecondary)
                )
            }
        }
    }
}

/**
 * 요일 헤더 (월간 뷰용)
 */
@Composable
private fun CalendarDayHeader(
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
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
    selectedDate: LocalDate?,
    today: LocalDate,
    datesWithSchedules: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        val hasSchedule = datesWithSchedules.contains(date)

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
 * - 선택된 날짜: accent 배경
 * - 오늘: accent 텍스트 + "오늘" 라벨 (항상 표시)
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
    val colors = FlitTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
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
            .padding(vertical = 2.dp),
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

        // 하단 영역: 오늘 라벨 또는 일정 dot
        Box(
            modifier = Modifier.height(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isToday) {
                Text(
                    text = "오늘",
                    color = colors.accent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (hasSchedule && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.textSecondary)
                )
            }
        }
    }
}
