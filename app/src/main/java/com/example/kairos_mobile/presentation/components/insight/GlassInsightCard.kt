package com.example.kairos_mobile.presentation.components.insight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.presentation.insight.InsightMode
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 미니멀한 글래스모피즘 메인 인사이트 카드
 * Pencil 디자인 기반 - 세련된 타이포그래피와 섬세한 상호작용
 */
@Composable
fun GlassInsightCard(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onModeSelected: (InsightMode) -> Unit,
    suggestedQuickTypes: List<InsightType> = emptyList(),
    onQuickTypeSelected: (InsightType) -> Unit = {},
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textQuaternaryColor = if (isDarkTheme) TextQuaternary else AiryTextQuaternary
    val cursorColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val dividerColor = if (isDarkTheme) GlassBorderDim else AiryGlassBorder.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardThemed(isDarkTheme = isDarkTheme)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 텍스트 입력 영역 (192dp 높이)
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = enabled && !isLoading,
            textStyle = TextStyle(
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 32.5.sp
            ),
            cursorBrush = SolidColor(cursorColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "무엇이든 입력하세요",
                            style = TextStyle(
                                color = textQuaternaryColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 32.5.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // 섬세한 구분선 (상단 테두리만)
        HorizontalDivider(
            color = dividerColor,
            thickness = 1.dp
        )

        // 하단 액션 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 모드 선택 버튼들 (이미지, 마이크, 링크)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassModeButton(
                    icon = Icons.Default.Image,
                    contentDescription = "이미지",
                    onClick = { onModeSelected(InsightMode.IMAGE) },
                    isDarkTheme = isDarkTheme
                )
                GlassModeButton(
                    icon = Icons.Default.Mic,
                    contentDescription = "음성",
                    onClick = { onModeSelected(InsightMode.VOICE) },
                    isDarkTheme = isDarkTheme
                )
                GlassModeButton(
                    icon = Icons.Default.Link,
                    contentDescription = "링크",
                    onClick = { onModeSelected(InsightMode.WEB_CLIP) },
                    isDarkTheme = isDarkTheme
                )
            }

            // 오른쪽: Capture 버튼 (Pill 형태)
            val buttonBgColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
            val buttonContentColor = if (isDarkTheme) TextPrimary else androidx.compose.ui.graphics.Color.White

            Button(
                onClick = onSubmit,
                enabled = enabled && text.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBgColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = buttonBgColor.copy(alpha = 0.45f),
                    disabledContentColor = buttonContentColor.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = buttonContentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capture",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 미니멀한 모드 선택 버튼 (32x32 원형)
 */
@Composable
private fun GlassModeButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val iconTint = if (isDarkTheme) TextTertiary else AiryTextSecondary

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .glassButtonThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(50))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}
