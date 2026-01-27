package com.example.kairos_mobile.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
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
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Archive 캡처 카드 (확장/축소 가능)
 */
@Composable
fun ArchiveCaptureCard(
    capture: Capture,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCaptureClick: () -> Unit,
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
            .clickable(onClick = onToggleExpand)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 헤더: 타입 + 시간 + 확장 아이콘
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
                // 타입 아이콘
                capture.classification?.type?.let { type ->
                    TypeIcon(type = type, isDarkTheme = isDarkTheme)
                }

                // 시간
                Text(
                    text = formatTime(capture.timestamp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimaryColor,
                    letterSpacing = 0.2.sp
                )
            }

            // 확장/축소 아이콘
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "축소" else "확장",
                tint = textTertiaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // 내용 미리보기 (항상 표시)
        Text(
            text = capture.content,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = textPrimaryColor,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        // 확장된 내용 (조건부 표시)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val dividerColor = if (isDarkTheme) GlassBorderDim else AiryGlassBorder
            val buttonBgColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
            val buttonContentColor = if (isDarkTheme) TextPrimary else androidx.compose.ui.graphics.Color.White

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(
                    color = dividerColor,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 상세 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 소스
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getSourceIcon(capture.source),
                            contentDescription = capture.source.name,
                            tint = textTertiaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = getSourceLabel(capture.source),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = textTertiaryColor,
                            letterSpacing = 0.1.sp
                        )
                    }

                    // 동기화 상태
                    SyncStatusBadge(syncStatus = capture.syncStatus.name, isDarkTheme = isDarkTheme)
                }

                // 타입 라벨 (있는 경우)
                capture.classification?.type?.let { type ->
                    val typeColor = getThemedTypeColor(type, isDarkTheme)
                    Surface(
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

                // 상세보기 버튼
                Button(
                    onClick = onCaptureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBgColor.copy(alpha = 0.8f),
                        contentColor = buttonContentColor
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text(
                        text = "상세보기",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

/**
 * 테마에 따른 타입 색상 반환
 */
private fun getThemedTypeColor(type: CaptureType, isDarkTheme: Boolean): androidx.compose.ui.graphics.Color {
    return if (isDarkTheme) {
        type.getColor()
    } else {
        when (type) {
            CaptureType.IDEA -> AiryIdeaColor
            CaptureType.SCHEDULE -> AiryMeetingColor
            CaptureType.TODO -> AiryTodoColor
            CaptureType.NOTE -> AirySaveColor
            CaptureType.QUICK_NOTE -> AiryTextTertiary
        }
    }
}

/**
 * 타입 아이콘
 */
@Composable
private fun TypeIcon(
    type: CaptureType,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        CaptureType.IDEA -> Icons.Default.Lightbulb
        CaptureType.SCHEDULE -> Icons.Default.CalendarToday
        CaptureType.TODO -> Icons.Default.CheckCircle
        CaptureType.NOTE -> Icons.Default.Bookmark
        CaptureType.QUICK_NOTE -> Icons.AutoMirrored.Filled.Note
    }

    Icon(
        imageVector = icon,
        contentDescription = type.getDisplayName(),
        tint = getThemedTypeColor(type, isDarkTheme),
        modifier = modifier.size(18.dp)
    )
}

/**
 * 동기화 상태 뱃지
 */
@Composable
private fun SyncStatusBadge(
    syncStatus: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val defaultColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val (text, color, icon) = when (syncStatus) {
        "SYNCED" -> Triple("동기화됨", if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF81C784) else AirySuccessColor, Icons.Default.CheckCircle)
        "PENDING" -> Triple("대기 중", if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFFFB74D) else AiryWarningColor, Icons.Default.Schedule)
        "FAILED" -> Triple("실패", if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFE57373) else AiryErrorColor, Icons.Default.Error)
        else -> Triple(syncStatus, defaultColor, Icons.Default.Info)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = color,
            letterSpacing = 0.1.sp
        )
    }
}

/**
 * 시간 포맷팅 (HH:mm)
 */
private fun formatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
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
