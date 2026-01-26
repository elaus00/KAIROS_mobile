package com.example.kairos_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.ui.theme.*

/**
 * Minimal Glassmorphism Effect Modifiers
 *
 * 세련된 미니멀 글래스모피즘 효과:
 * - 정밀한 투명도 제어
 * - 섬세한 테두리와 그림자
 * - 계층적 깊이 표현
 */

/**
 * Glass Panel Effect
 * - 가벼운 글래스 효과
 * - 네비게이션 바, 작은 패널에 사용
 */
fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 6.dp
) = this
    .shadow(elevation = shadowElevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.25f))
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Glass Card Effect
 * - 메인 입력 카드용 글래스 효과
 * - 섬세하고 정제된 외형
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassCard,
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 10.dp
) = this
    .shadow(elevation = shadowElevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.2f))
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Glass Button Effect
 * - 미니멀한 버튼 글래스 효과
 * - 호버 상태 없이 정적인 외형
 */
fun Modifier.glassButton(
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = GlassButton,
    borderColor: Color = GlassBorderDim,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 0.dp
) = this
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Glass Button Pressed Effect
 * - 버튼 눌림 상태용 글래스 효과
 */
fun Modifier.glassButtonPressed(
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = GlassButtonHover,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp
) = this
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)
