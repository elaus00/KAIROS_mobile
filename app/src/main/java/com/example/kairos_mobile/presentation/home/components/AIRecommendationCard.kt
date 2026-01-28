package com.example.kairos_mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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

/**
 * AI 추천 일정 카드 컴포넌트 (PRD v4.0)
 * 오늘의 다음 일정을 표시
 */
@Composable
fun AIRecommendationCard(
    schedule: Schedule?,
    todayScheduleCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    if (schedule == null && todayScheduleCount == 0) {
        // 일정이 없을 때
        EmptyScheduleCard(
            onClick = onClick,
            modifier = modifier
        )
    } else {
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
                Column(modifier = Modifier.weight(1f)) {
                    // 레이블
                    Text(
                        text = "다음 일정",
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (schedule != null) {
                        // 일정 시간
                        Text(
                            text = schedule.getFormattedTime(),
                            color = colors.text,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        // 일정 제목
                        Text(
                            text = schedule.title,
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    } else {
                        // 오늘 남은 일정 없음
                        Text(
                            text = "오늘 남은 일정 없음",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }

                    // 오늘 전체 일정 개수
                    if (todayScheduleCount > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "외 ${todayScheduleCount - 1}개",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // 화살표 아이콘
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "캘린더로 이동",
                        tint = colors.icon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 일정이 없을 때 표시되는 카드
 */
@Composable
private fun EmptyScheduleCard(
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "오늘 일정",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "일정이 없습니다",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "캘린더로 이동",
                    tint = colors.icon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
