package com.example.kairos_mobile.presentation.result.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.ui.components.glassPanelThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 확인 결과 카드 (신뢰도 80-95%)
 * "이대로 저장" + "수정" 버튼 표시
 */
@Composable
fun ConfirmResultCard(
    classification: Classification,
    content: String,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    isSaving: Boolean = false,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val warningColor = if (isDarkTheme) WarningOrange else AiryWarningOrange

    Column(
        modifier = modifier
            .glassPanelThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 상단: 신뢰도 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = warningColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "분류 결과 확인",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = warningColor
                )
            }

            Text(
                text = "${(classification.confidence * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = warningColor
            )
        }

        // 분류 결과
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 타입 칩
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = classification.type.getColor().copy(alpha = 0.15f)
            ) {
                Text(
                    text = classification.type.getDisplayName(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = classification.type.getColor(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Text(
                text = classification.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )

            // 태그
            if (classification.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    classification.tags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 12.sp,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 원본 콘텐츠 미리보기
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDarkTheme) GlassCard.copy(alpha = 0.5f) else AiryGlassCard.copy(alpha = 0.5f)
        ) {
            Text(
                text = content,
                fontSize = 13.sp,
                color = textSecondaryColor,
                maxLines = 3,
                modifier = Modifier.padding(12.dp)
            )
        }

        // 버튼 행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 수정 버튼
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = textPrimaryColor
                ),
                enabled = !isSaving
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "수정",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 확인 버튼
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "이대로 저장",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
