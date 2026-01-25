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
import com.example.kairos_mobile.ui.components.glassButton
import com.example.kairos_mobile.ui.theme.*

/**
 * 빠른 캡처 타입 선택 버튼들
 * (Idea, Meeting, To-do, Save)
 */
@Composable
fun QuickTypeButtons(
    onTypeSelected: (CaptureType) -> Unit,
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
            onClick = { onTypeSelected(CaptureType.IDEA) }
        )

        QuickTypeButton(
            icon = Icons.Default.CalendarToday,
            label = "Meeting",
            captureType = CaptureType.SCHEDULE,
            onClick = { onTypeSelected(CaptureType.SCHEDULE) }
        )

        QuickTypeButton(
            icon = Icons.Default.CheckCircle,
            label = "To-do",
            captureType = CaptureType.TODO,
            onClick = { onTypeSelected(CaptureType.TODO) }
        )

        QuickTypeButton(
            icon = Icons.Default.Bookmark,
            label = "Save",
            captureType = CaptureType.NOTE,
            onClick = { onTypeSelected(CaptureType.NOTE) }
        )
    }
}

/**
 * 개별 Quick Type 버튼
 */
@Composable
private fun QuickTypeButton(
    icon: ImageVector,
    label: String,
    captureType: CaptureType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .glassButton(shape = RoundedCornerShape(12.dp))
            .height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextDim,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = TextSecondary
            )
        }
    }
}
