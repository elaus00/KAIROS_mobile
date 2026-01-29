package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ScheduleCategory
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.LocalTime

/**
 * ScheduleTimeline 컴포넌트 (PRD v4.0)
 * 시간 기반 일정 목록 (타임라인 dot)
 *
 * @param schedules 일정 목록
 * @param onScheduleClick 일정 클릭 콜백
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = schedules,
                    key = { it.id }
                ) { schedule ->
                    ScheduleTimelineItem(
                        schedule = schedule,
                        isPast = schedule.time.isBefore(currentTime),
                        isFirst = schedules.first() == schedule,
                        isLast = schedules.last() == schedule,
                        onClick = { onScheduleClick(schedule) }
                    )
                }
            }
        }
    }
}

/**
 * 타임라인 일정 아이템
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
    val alpha = if (isPast) 0.5f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 타임라인 (세로선 + dot)
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 위쪽 선
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(colors.borderLight)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPast) colors.textMuted else colors.accent
                    )
            )

            // 아래쪽 선
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f, fill = false)
                        .defaultMinSize(minHeight = 40.dp)
                        .background(colors.borderLight)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 일정 내용
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(vertical = 4.dp)
        ) {
            // 시간
            Text(
                text = schedule.getFormattedTime(),
                color = if (isPast) colors.textMuted else colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 제목
            Text(
                text = schedule.title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 장소 (있는 경우)
            schedule.location?.let { location ->
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = location,
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 카테고리 chip
            Spacer(modifier = Modifier.height(4.dp))
            CategoryChip(category = schedule.category)
        }
    }
}

/**
 * 카테고리 Chip
 */
@Composable
private fun CategoryChip(
    category: ScheduleCategory,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.chipBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
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

/**
 * 일정 카드 (대체 스타일)
 */
@Composable
fun ScheduleCard(
    schedule: Schedule,
    isPast: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val alpha = if (isPast) 0.5f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .kairosCard()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 시간
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = schedule.getFormattedTime(),
                    color = if (isPast) colors.textMuted else colors.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 구분선
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(
                        if (isPast) colors.borderLight else colors.accent
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 내용
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                schedule.location?.let { location ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = location,
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
