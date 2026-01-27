package com.example.kairos_mobile.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 글래스모피즘 스타일의 외부 서비스 연동 카드
 */
@Composable
fun IntegrationCard(
    title: String,
    description: String,
    isConnected: Boolean,
    lastSyncTime: Long?,
    syncedCount: Int,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    isLoading: Boolean,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val textQuaternaryColor = if (isDarkTheme) TextQuaternary else AiryTextQuaternary
    val successColor = if (isDarkTheme) SuccessColor else AirySuccessColor
    val errorColor = if (isDarkTheme) ErrorColor else AiryErrorColor
    val buttonBgColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val buttonContentColor = if (isDarkTheme) TextPrimary else androidx.compose.ui.graphics.Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardThemed(isDarkTheme = isDarkTheme)
            .padding(20.dp)
    ) {
        // 헤더: 타이틀 + 연결 상태
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimaryColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = textTertiaryColor
                )
            }

            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "연동됨",
                    tint = successColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 동기화 정보 (연동된 경우에만 표시)
        AnimatedVisibility(visible = isConnected) {
            Column(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                if (lastSyncTime != null) {
                    Text(
                        text = "마지막 동기화: ${formatTimestamp(lastSyncTime)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        color = textQuaternaryColor
                    )
                }

                if (syncedCount > 0) {
                    Text(
                        text = "동기화된 항목: ${syncedCount}개",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        color = textQuaternaryColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 버튼 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = textTertiaryColor
                )
            } else if (isConnected) {
                // 연동된 상태: 동기화 + 연동 해제 버튼
                TextButton(
                    onClick = onSync,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "동기화",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textTertiaryColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onDisconnect,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "해제",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = errorColor
                    )
                }
            } else {
                // 미연동 상태: 연동하기 버튼
                Button(
                    onClick = onConnect,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBgColor,
                        contentColor = buttonContentColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "연동하기",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 타임스탬프를 사람이 읽기 쉬운 형식으로 변환
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "방금 전"
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        else -> {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
