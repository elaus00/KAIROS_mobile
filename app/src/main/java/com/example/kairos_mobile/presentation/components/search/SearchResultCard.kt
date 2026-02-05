package com.example.kairos_mobile.presentation.components.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 검색 결과 카드 (PRD v4.0)
 * 미니멀 모노크롬 디자인
 */
@Composable
fun SearchResultCard(
    capture: Capture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .kairosCard()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 헤더: 타입 + 시간
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 타입 뱃지
            capture.classification?.type?.let { type ->
                TypeBadge(type = type)
            }

            // 시간
            Text(
                text = formatTimestamp(capture.timestamp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textMuted,
                letterSpacing = 0.2.sp
            )
        }

        // 내용
        Text(
            text = capture.content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = colors.text,
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
                    imageVector = getSourceIcon(capture.source),
                    contentDescription = capture.source.name,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = getSourceLabel(capture.source),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textMuted,
                    letterSpacing = 0.1.sp
                )
            }

            // 동기화 상태
            SyncStatusBadge(syncStatus = capture.syncStatus.name)
        }
    }
}

/**
 * 타입 뱃지
 */
@Composable
private fun TypeBadge(
    type: CaptureType,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Surface(
        modifier = modifier,
        color = colors.chipBg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = type.getDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 동기화 상태 뱃지
 */
@Composable
private fun SyncStatusBadge(
    syncStatus: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val (text, color) = when (syncStatus) {
        "SYNCED" -> "동기화됨" to colors.success
        "PENDING" -> "대기 중" to colors.warning
        "FAILED" -> "실패" to colors.danger
        else -> syncStatus to colors.textMuted
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
private fun getSourceIcon(source: CaptureSource) = when (source) {
    CaptureSource.TEXT -> Icons.Default.TextFields
    CaptureSource.IMAGE -> Icons.Default.Image
    CaptureSource.VOICE -> Icons.Default.Mic
    CaptureSource.SHARE -> Icons.Default.Share
    CaptureSource.WEB_CLIP -> Icons.Default.Link
}

/**
 * 소스별 라벨 반환
 */
private fun getSourceLabel(source: CaptureSource) = when (source) {
    CaptureSource.TEXT -> "텍스트"
    CaptureSource.IMAGE -> "이미지"
    CaptureSource.VOICE -> "음성"
    CaptureSource.SHARE -> "공유"
    CaptureSource.WEB_CLIP -> "웹"
}
