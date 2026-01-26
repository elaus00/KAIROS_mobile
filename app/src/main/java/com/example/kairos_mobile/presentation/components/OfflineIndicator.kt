package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassPanel
import com.example.kairos_mobile.ui.theme.TextMuted
import com.example.kairos_mobile.ui.theme.WarningColor

/**
 * 글래스모피즘 스타일의 오프라인 인디케이터
 * 동기화 대기중인 항목 수를 표시
 */
@Composable
fun OfflineIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .glassPanel(shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = "동기화 대기",
            tint = WarningColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "동기화 대기: ${count}개",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
    }
}
