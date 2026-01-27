package com.example.kairos_mobile.presentation.components.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightSource
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 검색 결과 카드
 */
@Composable
fun SearchResultCard(
    insight: Insight,
    onClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardThemed(isDarkTheme = isDarkTheme)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 헤더: 타입 + 시간
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 타입 뱃지
            insight.classification?.type?.let { type ->
                TypeBadge(type = type, isDarkTheme = isDarkTheme)
            }

            // 시간
            Text(
                text = formatTimestamp(insight.timestamp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor,
                letterSpacing = 0.2.sp
            )
        }

        // 내용
        Text(
            text = insight.content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = textPrimaryColor,
            lineHeight = 22.sp,
            letterSpacing = 0.2.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // 푸터: 소스 + 동기화 상태
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 소스 아이콘
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getSourceIcon(insight.source),
                    contentDescription = insight.source.name,
                    tint = textTertiaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = getSourceLabel(insight.source),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = textTertiaryColor,
                    letterSpacing = 0.1.sp
                )
            }

            // 동기화 상태
            SyncStatusBadge(syncStatus = insight.syncStatus.name, isDarkTheme = isDarkTheme)
        }
    }
}

/**
 * 타입 뱃지 (테마 인식)
 */
@Composable
private fun TypeBadge(
    type: InsightType,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val typeColor = getThemedTypeColor(type, isDarkTheme)

    Surface(
        modifier = modifier,
        color = typeColor.copy(alpha = 0.15f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
    ) {
        Text(
            text = type.getDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = typeColor,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 동기화 상태 뱃지 (테마 인식)
 */
@Composable
private fun SyncStatusBadge(
    syncStatus: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val defaultColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val (text, color) = when (syncStatus) {
        "SYNCED" -> "동기화됨" to if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF81C784) else AirySuccessColor
        "PENDING" -> "대기 중" to if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFFFB74D) else AiryWarningColor
        "FAILED" -> "실패" to if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFE57373) else AiryErrorColor
        else -> syncStatus to defaultColor
    }

    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        letterSpacing = 0.1.sp,
        modifier = modifier
    )
}

/**
 * 테마에 따른 타입 색상 반환
 */
private fun getThemedTypeColor(type: InsightType, isDarkTheme: Boolean): androidx.compose.ui.graphics.Color {
    return if (isDarkTheme) {
        type.getColor()
    } else {
        when (type) {
            InsightType.IDEA -> AiryIdeaColor
            InsightType.SCHEDULE -> AiryMeetingColor
            InsightType.TODO -> AiryTodoColor
            InsightType.NOTE -> AirySaveColor
            InsightType.QUICK_NOTE -> AiryTextTertiary
        }
    }
}

/**
 * 타임스탬프 포맷팅
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "방금 전"
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        diff < 604800_000 -> "${diff / 86400_000}일 전"
        else -> {
            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

/**
 * 소스별 아이콘 반환
 */
private fun getSourceIcon(source: InsightSource) = when (source) {
    InsightSource.TEXT -> Icons.Default.TextFields
    InsightSource.IMAGE -> Icons.Default.Image
    InsightSource.VOICE -> Icons.Default.Mic
    InsightSource.SHARE -> Icons.Default.Share
    InsightSource.WEB_CLIP -> Icons.Default.Link
}

/**
 * 소스별 라벨 반환
 */
private fun getSourceLabel(source: InsightSource) = when (source) {
    InsightSource.TEXT -> "텍스트"
    InsightSource.IMAGE -> "이미지"
    InsightSource.VOICE -> "음성"
    InsightSource.SHARE -> "공유"
    InsightSource.WEB_CLIP -> "웹"
}
