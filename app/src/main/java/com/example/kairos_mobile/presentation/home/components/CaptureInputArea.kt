package com.example.kairos_mobile.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.presentation.components.common.KairosChip
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 캡처 입력 영역 컴포넌트 (PRD v4.0)
 * 하단 고정 입력창 + AI 분류 chip + 카메라 버튼
 */
@Composable
fun CaptureInputArea(
    inputText: String,
    characterCount: Int,
    maxCharacterCount: Int,
    suggestedType: InsightType?,
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 상단: 글자수 + AI 분류 chip (입력 시작 시에만 표시)
        AnimatedVisibility(
            visible = inputText.isNotEmpty(),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 글자 수
                Text(
                    text = "$characterCount / $maxCharacterCount",
                    color = if (characterCount > maxCharacterCount * 0.9)
                        colors.danger else colors.textMuted,
                    fontSize = 12.sp
                )

                // AI 분류 chip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
            }
        }

        // 입력 필드
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 카메라 버튼
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.accentBg)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "카메라",
                    tint = colors.icon,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 텍스트 입력
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accentBg)
                    .border(
                        width = 1.dp,
                        color = if (isFocused) colors.accent else colors.borderLight,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            onFocusChange(focusState.isFocused)
                        },
                    textStyle = TextStyle(
                        color = colors.text,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "떠오르는 생각을 캡처...",
                                    color = colors.placeholder,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // 전송 버튼
            AnimatedVisibility(
                visible = inputText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
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
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "전송",
                            tint = if (colors.isDark) colors.background else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * InsightType에 따른 표시 이름
 */
private fun getTypeDisplayName(type: InsightType): String {
    return when (type) {
        InsightType.TODO -> "할 일"
        InsightType.IDEA -> "아이디어"
        InsightType.NOTE -> "노트"
        InsightType.QUICK_NOTE -> "메모"
        InsightType.CLIP -> "클립"
        else -> "노트"
    }
}
