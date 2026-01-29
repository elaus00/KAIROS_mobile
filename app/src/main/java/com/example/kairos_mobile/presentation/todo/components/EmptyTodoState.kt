package com.example.kairos_mobile.presentation.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.*

/**
 * 빈 투두 상태
 */
@Composable
fun EmptyTodoState(
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = textTertiaryColor.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "할 일이 없습니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textTertiaryColor,
                letterSpacing = 0.3.sp
            )

            Text(
                text = "+ 버튼을 눌러 새로운 할 일을 추가하거나\n캡처에서 TODO로 분류해보세요",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = textTertiaryColor.copy(alpha = 0.7f),
                letterSpacing = 0.2.sp,
                lineHeight = 22.sp
            )
        }
    }
}
