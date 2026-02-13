package com.flit.app.presentation.calendar.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.presentation.calendar.ScheduleDisplayItem
import com.flit.app.presentation.components.common.SectionHeader
import com.flit.app.presentation.components.common.SwipeableCard
import com.flit.app.ui.theme.FlitTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * ScheduleTimeline 컴포넌트
 * 시간 기반 일정 목록 (타임라인 형태) + long press 드래그 재정렬
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleTimeline(
    schedules: List<ScheduleDisplayItem>,
    targetCalendarName: String? = null,
    onScheduleClick: (ScheduleDisplayItem) -> Unit,
    onScheduleDelete: (String) -> Unit,
    onReorder: (List<String>) -> Unit = {},
    modifier: Modifier = Modifier,
    onApproveSuggestion: (String) -> Unit = {},
    onRejectSuggestion: (String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // 드래그 상태 관리
    val currentList = remember(schedules) { mutableStateListOf(*schedules.toTypedArray()) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var measuredItemHeight by remember { mutableFloatStateOf(0f) }
    val spacingPx = with(density) { 12.dp.toPx() }

    Column(modifier = modifier) {
        SectionHeader(title = "일정", fontSize = 15.sp)

        if (currentList.isEmpty()) {
            ScheduleEmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                currentList.forEachIndexed { index, schedule ->
                    key(schedule.scheduleId) {
                        val isDragging = draggingIndex == index

                        // 날짜+시간 전체를 비교 (미래 일정이 dimmed 되지 않도록)
                        val now = LocalDateTime.now()
                        val scheduleDateTime = schedule.startTime?.let {
                            Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                        val isPast = scheduleDateTime?.isBefore(now) ?: false
                        val isFirst = index == 0
                        val isLast = index == currentList.lastIndex

                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .onSizeChanged { size ->
                                    if (measuredItemHeight == 0f) {
                                        measuredItemHeight = size.height.toFloat()
                                    }
                                }
                                .graphicsLayer {
                                    if (isDragging) {
                                        translationY = dragOffsetY
                                        shadowElevation = 8f
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                        alpha = 0.92f
                                    }
                                }
                        ) {
                            ScheduleTimelineItem(
                                schedule = schedule,
                                isPast = isPast,
                                isFirst = isFirst,
                                isLast = isLast,
                                targetCalendarName = targetCalendarName,
                                onClick = { onScheduleClick(schedule) },
                                onDelete = { onScheduleDelete(schedule.captureId) },
                                onApprove = { onApproveSuggestion(schedule.scheduleId) },
                                onReject = { onRejectSuggestion(schedule.scheduleId) },
                                onDragStart = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    draggingIndex = index
                                    dragOffsetY = 0f
                                },
                                onDrag = { deltaY ->
                                    dragOffsetY += deltaY
                                    val slotHeight = if (measuredItemHeight > 0f) measuredItemHeight + spacingPx else 80f
                                    val targetIndex = (index + (dragOffsetY / slotHeight).roundToInt())
                                        .coerceIn(0, currentList.size - 1)
                                    if (targetIndex != index && targetIndex != draggingIndex) {
                                        currentList.add(targetIndex, currentList.removeAt(draggingIndex))
                                        draggingIndex = targetIndex
                                        dragOffsetY = 0f
                                    }
                                },
                                onDragEnd = {
                                    draggingIndex = -1
                                    dragOffsetY = 0f
                                    onReorder(currentList.map { it.scheduleId })
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 타임라인 일정 아이템
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ScheduleTimelineItem(
    schedule: ScheduleDisplayItem,
    isPast: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    targetCalendarName: String? = null,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, offset ->
                        change.consume()
                        onDrag(offset.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        verticalAlignment = Alignment.Top
    ) {
        // 왼쪽: 시간 + 타임라인
        Row(
            modifier = Modifier.width(60.dp),
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

        Spacer(modifier = Modifier.width(8.dp))

        // 오른쪽: 일정 카드 (스와이프 삭제)
        SwipeableCard(
            onDismiss = onDelete,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            ScheduleCard(
                schedule = schedule,
                isPast = isPast,
                targetCalendarName = targetCalendarName,
                onClick = onClick,
                onApprove = onApprove,
                onReject = onReject
            )
        }
    }
}

/**
 * 일정 카드
 */
@Composable
private fun ScheduleCard(
    schedule: ScheduleDisplayItem,
    isPast: Boolean,
    targetCalendarName: String? = null,
    onClick: () -> Unit,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

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
                            text = if (targetCalendarName != null) "'${targetCalendarName}'에 추가"
                                else "기기 캘린더에 추가",
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
    val colors = FlitTheme.colors

    val (label, badgeColor) = when (status) {
        CalendarSyncStatus.SYNCED -> "캘린더 동기화됨" to colors.success
        CalendarSyncStatus.SUGGESTION_PENDING -> "승인 대기" to colors.warning
        CalendarSyncStatus.SYNC_FAILED -> "실패" to colors.danger
        CalendarSyncStatus.REJECTED -> "거부됨" to colors.textMuted
        CalendarSyncStatus.NOT_LINKED -> return  // 배지 없음
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 시간 포맷 (epoch ms → "HH:mm" 또는 "종일")
 */
@RequiresApi(Build.VERSION_CODES.O)
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
    val colors = FlitTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "오늘은 일정이 없어요",
                color = colors.textMuted,
                fontSize = 14.sp
            )
        }
    }
}
