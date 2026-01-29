package com.example.kairos_mobile.presentation.result.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.ui.components.glassPanelThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 자동저장 결과 카드 (신뢰도 95% 이상)
 * 3초 프로그레스 바와 함께 자동저장 진행
 */
@Composable
fun AutoSaveResultCard(
    classification: Classification,
    content: String,
    progress: Float,
    countdown: Int,
    onEdit: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 50),
        label = "autoSaveProgress"
    )

    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val successColor = if (isDarkTheme) SuccessGreen else AirySuccessGreen

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
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = successColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "높은 신뢰도",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = successColor
                )
            }

            Text(
                text = "${(classification.confidence * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = successColor
            )
        }

        // 분류 결과
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = classification.type.getDisplayName(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )

            Text(
                text = classification.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondaryColor,
                maxLines = 2
            )

            // 태그
            if (classification.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    classification.tags.take(3).forEach { tag ->
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

        // 프로그레스 바
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = successColor,
                trackColor = successColor.copy(alpha = 0.2f)
            )

            Text(
                text = "${countdown}초 후 자동 저장",
                fontSize = 13.sp,
                color = textSecondaryColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // 수정 버튼
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = textPrimaryColor
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "수정하기",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
