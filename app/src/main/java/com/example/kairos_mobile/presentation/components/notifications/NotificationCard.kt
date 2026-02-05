package com.example.kairos_mobile.presentation.components.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.model.NotificationType
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 알림 카드 컴포넌트 (PRD v4.0)
 */
@Composable
fun NotificationCard(
    notification: Notification,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 헤더: 아이콘 + 제목 + 읽음 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 타입별 아이콘
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )

                // 제목
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notification.isRead) colors.textSecondary else colors.text,
                    letterSpacing = 0.2.sp
                )
            }

            // 읽지 않음 표시
            if (!notification.isRead) {
                Surface(
                    color = colors.accent,
                    shape = CircleShape,
                    modifier = Modifier.size(6.dp)
                ) {}
            }
        }

        // 메시지
        Text(
            text = notification.message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = if (notification.isRead) colors.textMuted else colors.textSecondary,
            lineHeight = 18.sp,
            letterSpacing = 0.2.sp
        )

        // 시간
        Text(
            text = formatNotificationTime(notification.timestamp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textMuted.copy(alpha = 0.7f),
            letterSpacing = 0.1.sp
        )
    }
}

/**
 * 알림 타입별 아이콘 반환
 */
private fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.CAPTURE_SAVED, NotificationType.CAPTURE_COMPLETE -> Icons.Default.CheckCircle
    NotificationType.SYNC_COMPLETED, NotificationType.SYNC_COMPLETE -> Icons.Default.CloudDone
    NotificationType.SYNC_FAILED -> Icons.Default.ErrorOutline
    NotificationType.AI_PROCESSING, NotificationType.AI_PROCESSING_COMPLETE -> Icons.Default.AutoAwesome
    NotificationType.REMINDER -> Icons.Default.Notifications
    NotificationType.SYSTEM, NotificationType.INFO -> Icons.Default.Info
}

/**
 * 알림 타입별 색상 반환
 */
@Composable
private fun getNotificationColor(type: NotificationType) = when (type) {
    NotificationType.CAPTURE_SAVED, NotificationType.CAPTURE_COMPLETE,
    NotificationType.SYNC_COMPLETED, NotificationType.SYNC_COMPLETE ->
        KairosTheme.colors.success

    NotificationType.SYNC_FAILED ->
        KairosTheme.colors.danger

    NotificationType.AI_PROCESSING, NotificationType.AI_PROCESSING_COMPLETE,
    NotificationType.REMINDER ->
        KairosTheme.colors.warning

    NotificationType.SYSTEM, NotificationType.INFO ->
        KairosTheme.colors.textSecondary
}

/**
 * 알림 시간 포맷팅
 */
private fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        minutes < 1 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days == 1L -> "어제"
        days < 7 -> "${days}일 전"
        else -> {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
