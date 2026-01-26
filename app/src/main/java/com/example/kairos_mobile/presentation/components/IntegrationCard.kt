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
import com.example.kairos_mobile.ui.components.glassCard
import com.example.kairos_mobile.ui.theme.ErrorColor
import com.example.kairos_mobile.ui.theme.PrimaryNavy
import com.example.kairos_mobile.ui.theme.SuccessColor
import com.example.kairos_mobile.ui.theme.TextPrimary
import com.example.kairos_mobile.ui.theme.TextQuaternary
import com.example.kairos_mobile.ui.theme.TextTertiary
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
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
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = TextTertiary
                )
            }

            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "연동됨",
                    tint = SuccessColor,
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
                        color = TextQuaternary
                    )
                }

                if (syncedCount > 0) {
                    Text(
                        text = "동기화된 항목: ${syncedCount}개",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        color = TextQuaternary,
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
                    color = TextTertiary
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
                        color = TextTertiary
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
                        color = ErrorColor
                    )
                }
            } else {
                // 미연동 상태: 연동하기 버튼
                Button(
                    onClick = onConnect,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNavy,
                        contentColor = TextPrimary
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
