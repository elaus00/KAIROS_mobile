package com.example.kairos_mobile.presentation.components.insight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 성공 피드백 컴포넌트
 * 인사이트 저장 성공 시 표시
 */
@Composable
fun SuccessFeedback(
    isOffline: Boolean = false,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val iconColor = if (isOffline) {
        if (isDarkTheme) TextTertiary else AiryTextTertiary
    } else {
        if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF81C784) else AirySuccessColor
    }

    Surface(
        modifier = modifier
            .glassCardThemed(isDarkTheme = isDarkTheme)
            .padding(24.dp),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Check,
                contentDescription = if (isOffline) "오프라인 저장" else "저장 완료",
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = if (isOffline) "오프라인 저장됨" else "저장 완료",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimaryColor,
                letterSpacing = 0.2.sp
            )
            if (isOffline) {
                Text(
                    text = "네트워크 연결 시 동기화됩니다",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDarkTheme) TextTertiary else AiryTextTertiary,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}
