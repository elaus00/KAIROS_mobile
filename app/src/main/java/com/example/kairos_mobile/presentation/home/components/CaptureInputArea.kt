package com.example.kairos_mobile.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.presentation.components.common.KairosChip
import com.example.kairos_mobile.ui.theme.KairosTheme
import androidx.compose.material3.Text

/**
 * 캡처 입력 영역 컴포넌트 (PRD v4.0)
 * 디자인 시안 반영: 전체 너비 입력창, 버튼 숨김/표시
 */
@Composable
fun CaptureInputArea(
    inputText: String,
    characterCount: Int,
    maxCharacterCount: Int,
    suggestedType: CaptureType?,
    isClassifying: Boolean,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    var isFocused by remember { mutableStateOf(false) }
    val hasText = inputText.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 입력 상자 (디자인: 전체 너비, 큰 영역)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.card)
                .border(
                    width = 1.dp,
                    color = if (isFocused) colors.border else colors.borderLight,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // 텍스트 입력 영역
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 56.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        onFocusChange(focusState.isFocused)
                    },
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "떠오르는 생각을 캡처하세요...",
                                color = colors.placeholder,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 하단 영역: AI 분류칩 + 버튼들 (항상 표시, 디자인 시안 반영)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: AI 분류 칩 또는 로딩 (입력 시에만)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasText) {
                        if (isClassifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.textMuted
                            )
                        } else if (suggestedType != null) {
                            KairosChip(
                                text = getTypeDisplayName(suggestedType),
                                selected = true
                            )
                        }

                        // 글자수 (90% 이상일 때만 표시)
                        if (characterCount > maxCharacterCount * 0.9) {
                            Text(
                                text = "$characterCount/$maxCharacterCount",
                                color = colors.danger,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 오른쪽: 전송 버튼 (항상 표시)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) colors.accent
                            else colors.accentBg
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isSubmitting && inputText.isNotBlank()
                        ) { onSubmit() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (colors.isDark) colors.background else Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = if (inputText.isNotBlank()) {
                                if (colors.isDark) colors.background else Color.White
                            } else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * CaptureType에 따른 표시 이름
 */
private fun getTypeDisplayName(type: CaptureType): String {
    return when (type) {
        CaptureType.IDEA -> "아이디어"
        CaptureType.SCHEDULE -> "일정"
        CaptureType.TODO -> "할 일"
        CaptureType.NOTE -> "노트"
        CaptureType.QUICK_NOTE -> "메모"
        CaptureType.CLIP -> "클립"
    }
}
