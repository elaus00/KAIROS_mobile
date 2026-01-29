package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * WeekPicker 컴포넌트 (PRD v4.0)
 * 주간 날짜 선택 (pill 스타일)
 *
 * @param selectedDate 선택된 날짜
 * @param datesWithSchedules 일정이 있는 날짜 목록 (dot 표시용)
 * @param onDateSelected 날짜 선택 콜백
 */
@Composable
fun WeekPicker(
    selectedDate: LocalDate,
    datesWithSchedules: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val today = LocalDate.now()

    // 선택된 날짜가 포함된 주의 시작일 (월요일)
    val startOfWeek = selectedDate.minusDays(
        (selectedDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
    )

    // 주간 날짜 리스트
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDates.forEach { date ->
            WeekDayItem(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                hasSchedule = datesWithSchedules.contains(date),
                onClick = { onDateSelected(date) }
            )
        }
    }
}

/**
 * 개별 요일 아이템
 */
@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 애니메이션 배경색
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            isToday -> colors.accentBg
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    // 텍스트 색상
    val textColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        isToday -> colors.accent
        else -> colors.text
    }

    val dayTextColor = when {
        isSelected -> if (colors.isDark) colors.background else Color.White
        else -> colors.textMuted
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요일 (월, 화, ...)
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                .replace("요일", ""),
            color = dayTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 날짜
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 일정 dot 표시
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    when {
                        hasSchedule && isSelected -> if (colors.isDark) colors.background else Color.White
                        hasSchedule -> colors.accent
                        else -> Color.Transparent
                    }
                )
        )
    }
}

/**
 * 요일 헤더 Row
 */
@Composable
fun WeekDayHeader(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEach { day ->
            Text(
                text = day,
                color = colors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
