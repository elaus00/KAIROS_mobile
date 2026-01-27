package com.example.kairos_mobile.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
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
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.presentation.capture.CaptureMode
import com.example.kairos_mobile.ui.components.glassButtonThemed
import com.example.kairos_mobile.ui.components.glassCardThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 미니멀한 글래스모피즘 메인 캡처 카드
 * 세련된 타이포그래피와 섬세한 상호작용
 *
 * Phase 3 개선: 동적 QuickTypeButtons 통합
 */
@Composable
fun GlassCaptureCard(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onModeSelected: (CaptureMode) -> Unit,
    suggestedQuickTypes: List<CaptureType> = emptyList(),
    onQuickTypeSelected: (CaptureType) -> Unit = {},
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textTertiaryColor = if (isDarkTheme) TextTertiary else AiryTextTertiary
    val cursorColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val dividerColor = if (isDarkTheme) GlassBorderDim else AiryGlassBorder

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardThemed(isDarkTheme = isDarkTheme)
            .padding(28.dp)
    ) {
        // 텍스트 입력 영역
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = enabled && !isLoading,
            textStyle = TextStyle(
                color = textPrimaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 28.sp,
                letterSpacing = 0.3.sp
            ),
            cursorBrush = SolidColor(cursorColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "무엇이든 캡처하세요…",
                            style = TextStyle(
                                color = textTertiaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                lineHeight = 28.sp,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // QuickTypeButtons (동적 표시)
        AnimatedVisibility(
            visible = suggestedQuickTypes.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                // 섬세한 구분선
                HorizontalDivider(
                    color = dividerColor,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Quick Type Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    suggestedQuickTypes.forEach { type ->
                        QuickTypeButton(
                            icon = getIconForType(type),
                            label = getLabelForType(type),
                            onClick = { onQuickTypeSelected(type) },
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }

        // 섬세한 구분선
        HorizontalDivider(
            color = dividerColor,
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        // 하단 액션 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지 모드 선택 버튼만 유지
            GlassModeButton(
                icon = Icons.Default.Image,
                contentDescription = "이미지",
                onClick = { onModeSelected(CaptureMode.IMAGE) },
                isDarkTheme = isDarkTheme
            )

            // Capture 버튼 (테마에 따른 색상)
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
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = buttonContentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capture",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 미니멀한 모드 선택 버튼
 */
@Composable
private fun GlassModeButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val iconTint = if (isDarkTheme) TextTertiary else AiryTextTertiary

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(34.dp)
            .glassButtonThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(50))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
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

/**
 * CaptureType에 맞는 아이콘 반환
 */
private fun getIconForType(type: CaptureType): ImageVector {
    return when (type) {
        CaptureType.IDEA -> Icons.Default.Lightbulb
        CaptureType.SCHEDULE -> Icons.Default.CalendarToday
        CaptureType.TODO -> Icons.Default.CheckCircle
        CaptureType.NOTE -> Icons.Default.Bookmark
        CaptureType.QUICK_NOTE -> Icons.AutoMirrored.Filled.Note
    }
}

/**
 * CaptureType에 맞는 라벨 반환
 */
private fun getLabelForType(type: CaptureType): String {
    return when (type) {
        CaptureType.IDEA -> "Idea"
        CaptureType.SCHEDULE -> "Meeting"
        CaptureType.TODO -> "To-do"
        CaptureType.NOTE -> "Save"
        CaptureType.QUICK_NOTE -> "Quick"
    }
}
