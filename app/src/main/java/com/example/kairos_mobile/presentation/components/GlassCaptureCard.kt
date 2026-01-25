package com.example.kairos_mobile.presentation.components

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
import com.example.kairos_mobile.presentation.capture.CaptureMode
import com.example.kairos_mobile.ui.components.glassButton
import com.example.kairos_mobile.ui.components.glassCard
import com.example.kairos_mobile.ui.theme.*

/**
 * Glassmorphism 스타일의 메인 캡처 카드
 */
@Composable
fun GlassCaptureCard(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onModeSelected: (CaptureMode) -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
            .padding(24.dp)
    ) {
        // 텍스트 입력 영역
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = enabled && !isLoading,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 32.sp,
                letterSpacing = 0.5.sp
            ),
            cursorBrush = SolidColor(PrimaryNavy),
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "무엇이든 입력하세요...",
                            style = TextStyle(
                                color = TextTertiary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                lineHeight = 32.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // 구분선
        Divider(
            color = GlassBorderDim,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 하단 액션 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 모드 선택 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassModeButton(
                    icon = Icons.Default.Image,
                    contentDescription = "이미지 캡처",
                    onClick = { onModeSelected(CaptureMode.IMAGE) }
                )

                GlassModeButton(
                    icon = Icons.Default.Mic,
                    contentDescription = "음성 캡처",
                    onClick = { onModeSelected(CaptureMode.VOICE) }
                )

                GlassModeButton(
                    icon = Icons.Default.Link,
                    contentDescription = "웹 클립",
                    onClick = { onModeSelected(CaptureMode.WEB_CLIP) }
                )
            }

            // Capture 버튼
            Button(
                onClick = onSubmit,
                enabled = enabled && text.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavy,
                    contentColor = TextPrimary,
                    disabledContainerColor = PrimaryNavy.copy(alpha = 0.5f),
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
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
                            letterSpacing = 0.5.sp
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
 * Glass 스타일의 모드 선택 버튼
 */
@Composable
private fun GlassModeButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .glassButton(shape = RoundedCornerShape(50))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
