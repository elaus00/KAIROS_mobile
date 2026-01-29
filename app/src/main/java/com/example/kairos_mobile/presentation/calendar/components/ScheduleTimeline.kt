package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ScheduleCategory
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.LocalTime

/**
 * ScheduleTimeline 컴포넌트 (Reference 디자인)
 * 시간 기반 일정 목록 (타임라인 형태)
 */
@Composable
fun ScheduleTimeline(
    schedules: List<Schedule>,
    onScheduleClick: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val currentTime = remember { LocalTime.now() }

    Column(modifier = modifier) {
        SectionHeader(title = "일정")

        if (schedules.isEmpty()) {
            ScheduleEmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                schedules.forEachIndexed { index, schedule ->
                    val isPast = schedule.time.isBefore(currentTime)
                    val isFirst = index == 0
                    val isLast = index == schedules.lastIndex

                    ScheduleTimelineItem(
                        schedule = schedule,
                        isPast = isPast,
                        isFirst = isFirst,
                        isLast = isLast,
                        onClick = { onScheduleClick(schedule) }
                    )
                }
            }
        }
    }
}

/**
 * 타임라인 일정 아이템 (Reference 디자인)
 */
@Composable
private fun ScheduleTimelineItem(
    schedule: Schedule,
    isPast: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // 왼쪽: 시간 + 타임라인
        Row(
            modifier = Modifier.width(70.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 시간 텍스트
            Text(
                text = schedule.getFormattedTime(),
                color = if (isPast) colors.textMuted else colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(48.dp)
            )

            // 타임라인 (세로선 + dot)
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 위쪽 선
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(8.dp)
                        .background(if (isFirst) Color.Transparent else colors.border)
                )

                // Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPast) colors.border else colors.accent)
                )

                // 아래쪽 선
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(if (isLast) Color.Transparent else colors.border)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 오른쪽: 일정 카드
        ScheduleCard(
            schedule = schedule,
            isPast = isPast,
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * 일정 카드 (Reference 디자인)
 */
@Composable
private fun ScheduleCard(
    schedule: Schedule,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 지난 일정은 배경색 다르게
    val cardBackground = if (isPast) colors.accentBg else colors.card

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp, 14.dp)
    ) {
        Column {
            // 제목 + 카테고리 chip
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.title,
                    color = if (isPast) colors.textMuted else colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 카테고리 chip
                CategoryChip(
                    category = schedule.category,
                    isPast = isPast
                )
            }

            // 장소 (있는 경우)
            schedule.location?.let { location ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = location,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 카테고리 Chip (Reference 디자인)
 */
@Composable
private fun CategoryChip(
    category: ScheduleCategory,
    isPast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.chipBg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.getDisplayName(),
            color = colors.chipText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 일정 없음 상태
 */
@Composable
private fun ScheduleEmptyState(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "일정이 없습니다",
            color = colors.textMuted,
            fontSize = 14.sp
        )
    }
}
