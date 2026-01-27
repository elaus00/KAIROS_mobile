package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 빠른 캡처 타입 선택 버튼들
 * (Idea, Meeting, To-do, Save)
 */
@Composable
fun QuickTypeButtons(
    onTypeSelected: (CaptureType) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickTypeButton(
            icon = Icons.Default.Lightbulb,
            label = "Idea",
            captureType = CaptureType.IDEA,
            onClick = { onTypeSelected(CaptureType.IDEA) },
            isDarkTheme = isDarkTheme
        )

        QuickTypeButton(
            icon = Icons.Default.CalendarToday,
            label = "Meeting",
            captureType = CaptureType.SCHEDULE,
            onClick = { onTypeSelected(CaptureType.SCHEDULE) },
            isDarkTheme = isDarkTheme
        )

        QuickTypeButton(
            icon = Icons.Default.CheckCircle,
            label = "To-do",
            captureType = CaptureType.TODO,
            onClick = { onTypeSelected(CaptureType.TODO) },
            isDarkTheme = isDarkTheme
        )

        QuickTypeButton(
            icon = Icons.Default.Bookmark,
            label = "Save",
            captureType = CaptureType.NOTE,
            onClick = { onTypeSelected(CaptureType.NOTE) },
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * 미니멀한 Quick Type 버튼
 */
@Composable
private fun QuickTypeButton(
    icon: ImageVector,
    label: String,
    captureType: CaptureType,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    Button(
        onClick = onClick,
        modifier = modifier
            .glassButtonThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(10.dp))
            .height(34.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}
