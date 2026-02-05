package com.example.kairos_mobile.presentation.result.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.components.kairosElevatedCard
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 타입 선택 카드 (신뢰도 80% 미만)
 * 사용자가 직접 유형 선택
 */
@Composable
fun TypeSelectionCard(
    content: String,
    selectedType: CaptureType?,
    onTypeSelected: (CaptureType) -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // PRD에서 정의된 타입들 (SCHEDULE 제외)
    val availableTypes = listOf(
        CaptureType.IDEA,
        CaptureType.TODO,
        CaptureType.NOTE,
        CaptureType.QUICK_NOTE
    )

    Column(
        modifier = modifier
            .kairosElevatedCard(shape = RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 상단: 안내 메시지
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "유형을 선택해주세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text
            )
        }

        // 원본 콘텐츠 미리보기
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.accentBg
        ) {
            Text(
                text = content,
                fontSize = 14.sp,
                color = colors.textSecondary,
                maxLines = 4,
                modifier = Modifier.padding(12.dp)
            )
        }

        // 타입 선택 그리드 (2x2)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                availableTypes.take(2).forEach { type ->
                    TypeSelectionButton(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                availableTypes.drop(2).forEach { type ->
                    TypeSelectionButton(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 확인 버튼
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.card,
                disabledContainerColor = colors.accentBg,
                disabledContentColor = colors.textMuted
            ),
            enabled = selectedType != null && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.card,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (selectedType != null) "${selectedType.getDisplayName()}(으)로 저장" else "유형을 선택하세요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 타입 선택 버튼
 */
@Composable
private fun TypeSelectionButton(
    type: CaptureType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor = if (isSelected) colors.accentBg else colors.chipBg
    val textColor = if (isSelected) colors.text else colors.textSecondary

    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isSelected) BorderStroke(2.dp, colors.accent) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = type.getIcon(),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.getDisplayName(),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * CaptureType에 대한 아이콘 확장 함수
 */
private fun CaptureType.getIcon(): ImageVector {
    return when (this) {
        CaptureType.IDEA -> Icons.Default.Lightbulb
        CaptureType.SCHEDULE -> Icons.Default.Event
        CaptureType.TODO -> Icons.Default.CheckCircle
        CaptureType.NOTE -> Icons.Default.Description
        CaptureType.QUICK_NOTE -> Icons.Default.FlashOn
        CaptureType.CLIP -> Icons.Default.Link
    }
}
