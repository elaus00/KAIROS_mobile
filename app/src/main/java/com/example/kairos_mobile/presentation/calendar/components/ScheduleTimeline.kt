package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.presentation.calendar.ScheduleDisplayItem
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ScheduleTimeline 컴포넌트
 * 시간 기반 일정 목록 (타임라인 형태)
 */
@Composable
fun ScheduleTimeline(
    schedules: List<ScheduleDisplayItem>,
    onScheduleClick: (ScheduleDisplayItem) -> Unit,
    onScheduleDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    onApproveSuggestion: (String) -> Unit = {},
    onRejectSuggestion: (String) -> Unit = {}
) {
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
                    // 날짜+시간 전체를 비교 (미래 일정이 dimmed 되지 않도록)
                    val now = LocalDateTime.now()
                    val scheduleDateTime = schedule.startTime?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    }
                    val isPast = scheduleDateTime?.isBefore(now) ?: false
                    val isFirst = index == 0
                    val isLast = index == schedules.lastIndex

                    ScheduleTimelineItem(
                        schedule = schedule,
                        isPast = isPast,
                        isFirst = isFirst,
                        isLast = isLast,
                        onClick = { onScheduleClick(schedule) },
                        onDelete = { onScheduleDelete(schedule.captureId) },
                        onApprove = { onApproveSuggestion(schedule.scheduleId) },
                        onReject = { onRejectSuggestion(schedule.scheduleId) }
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
    schedule: ScheduleDisplayItem,
    isPast: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
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
                text = formatTime(schedule.startTime, schedule.isAllDay),
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
            onDelete = onDelete,
            onApprove = onApprove,
            onReject = onReject,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * 일정 카드
 */
@Composable
private fun ScheduleCard(
    schedule: ScheduleDisplayItem,
    isPast: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.title,
                    color = if (isPast) colors.textMuted else colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 동기화 상태 배지
                SyncStatusBadge(status = schedule.calendarSyncStatus)

                Spacer(modifier = Modifier.width(8.dp))

                // 삭제 아이콘 - 48dp 터치 타겟 확보
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "일정 삭제",
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

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

            // SUGGESTION_PENDING 시 승인/거부 버튼
            if (schedule.calendarSyncStatus == CalendarSyncStatus.SUGGESTION_PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accent)
                            .clickable { onApprove() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "캘린더 추가",
                            color = if (colors.isDark) colors.background else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.chipBg)
                            .clickable { onReject() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "무시",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 동기화 상태 배지
 */
@Composable
private fun SyncStatusBadge(
    status: CalendarSyncStatus,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val (label, badgeColor) = when (status) {
        CalendarSyncStatus.SYNCED -> "동기화됨" to colors.success
        CalendarSyncStatus.SUGGESTION_PENDING -> "제안" to colors.warning
        CalendarSyncStatus.SYNC_FAILED -> "실패" to colors.danger
        CalendarSyncStatus.REJECTED -> "거부됨" to colors.textMuted
        CalendarSyncStatus.NOT_LINKED -> return  // 배지 없음
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 시간 포맷 (epoch ms → "HH:mm" 또는 "종일")
 */
private fun formatTime(epochMs: Long?, isAllDay: Boolean): String {
    if (isAllDay) return "종일"
    if (epochMs == null) return ""
    val time = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
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
