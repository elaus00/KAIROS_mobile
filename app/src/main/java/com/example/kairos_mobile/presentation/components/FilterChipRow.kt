package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 필터 칩 행
 * CaptureType 필터를 표시
 */
@Composable
fun FilterChipRow(
    selectedTypes: Set<CaptureType>,
    onTypeToggle: (CaptureType) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 모든 CaptureType에 대한 칩
        CaptureType.values().forEach { type ->
            FilterChip(
                type = type,
                isSelected = type in selectedTypes,
                onClick = { onTypeToggle(type) },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * 개별 필터 칩
 */
@Composable
private fun FilterChip(
    type: CaptureType,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val typeColor = getThemedTypeColor(type, isDarkTheme)
    val unselectedColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    val backgroundColor = if (isSelected) {
        typeColor.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        typeColor
    } else {
        unselectedColor
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .glassButtonThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(20.dp))
            .height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = type.getDisplayName(),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * 테마에 따른 타입 색상 반환
 */
private fun getThemedTypeColor(type: CaptureType, isDarkTheme: Boolean): Color {
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
