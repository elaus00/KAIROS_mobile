package com.example.kairos_mobile.presentation.components.insight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 오프라인 인디케이터 컴포넌트
 * 대기 중인 인사이트 개수를 표시
 */
@Composable
fun OfflineIndicator(
    count: Int,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val iconColor = if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFFFFB74D) else AiryWarningColor

    Surface(
        modifier = modifier
            .glassButtonThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(20.dp)),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "오프라인",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$count 대기 중",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                letterSpacing = 0.1.sp
            )
        }
    }
}
