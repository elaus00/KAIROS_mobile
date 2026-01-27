package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 알림 카드 컴포넌트
 */
@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val accentColor = if (isDarkTheme) AccentBlue else AiryAccentBlue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardThemed(isDarkTheme = isDarkTheme)
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
                    tint = getNotificationColor(notification.type, isDarkTheme),
                    modifier = Modifier.size(20.dp)
                )

                // 제목
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notification.isRead) textSecondaryColor else textPrimaryColor,
                    letterSpacing = 0.2.sp
                )
            }

            // 읽지 않음 표시
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = accentColor,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        modifier = Modifier.size(6.dp)
                    ) {}
                }
            }
        }

        // 메시지
        Text(
            text = notification.message,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = if (notification.isRead) textTertiaryColor else textSecondaryColor,
            lineHeight = 18.sp,
            letterSpacing = 0.2.sp
        )

        // 시간
        Text(
            text = formatNotificationTime(notification.timestamp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = textTertiaryColor.copy(alpha = 0.7f),
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
 * 알림 타입별 색상 반환 (테마 인식)
 */
@Composable
private fun getNotificationColor(type: NotificationType, isDarkTheme: Boolean) = when (type) {
    NotificationType.CAPTURE_SAVED, NotificationType.CAPTURE_COMPLETE ->
        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF81C784) else AirySuccessColor
    NotificationType.SYNC_COMPLETED, NotificationType.SYNC_COMPLETE ->
        if (isDarkTheme) AccentBlue else AiryAccentBlue
    NotificationType.SYNC_FAILED ->
        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFE57373) else AiryErrorColor
    NotificationType.AI_PROCESSING, NotificationType.AI_PROCESSING_COMPLETE ->
        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFBA68C8) else AirySaveColor
    NotificationType.REMINDER ->
        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFFFB74D) else AiryWarningColor
    NotificationType.SYSTEM, NotificationType.INFO ->
        if (isDarkTheme) TextSecondary else AiryTextSecondary
}

/**
 * 알림 시간 포맷팅
 * "방금 전", "5분 전", "1시간 전", "어제", "2026-01-23"
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
