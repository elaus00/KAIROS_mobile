package com.example.kairos_mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * AI 추천 일정 카드 컴포넌트 (PRD v4.0)
 * 디자인 시안 반영: 시계 아이콘 + "오늘의 중요 일정" + 시간 포맷
 */
@Composable
fun AIRecommendationCard(
    schedule: Schedule?,
    todayScheduleCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .kairosCard()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 시계 아이콘 (원형 배경)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // 레이블: "오늘의 중요 일정"
                    Text(
                        text = "오늘의 중요 일정",
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (schedule != null) {
                        // 일정 제목 + 시간 포맷: "오후 2시 미팅 · 14:00"
                        Text(
                            text = formatScheduleDisplay(schedule),
                            color = colors.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "일정이 없습니다",
                            color = colors.textSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 화살표 아이콘 (배경 없이)
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "캘린더로 이동",
                tint = colors.textMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 일정 표시 포맷: "오후 2시 미팅 · 14:00"
 */
private fun formatScheduleDisplay(schedule: Schedule): String {
    val time = schedule.time
    val hour = time.hour
    val minute = time.minute

    // 한국어 시간 포맷 (오전/오후)
    val koreanPeriod = if (hour < 12) "오전" else "오후"
    val koreanHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

    // 24시간 포맷
    val timeFormat = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    return "${koreanPeriod} ${koreanHour}시 ${schedule.title} · $timeFormat"
}
