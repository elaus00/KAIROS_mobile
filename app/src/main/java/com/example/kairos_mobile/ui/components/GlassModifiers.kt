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
 * Glassmorphism Effect Modifiers
 *
 * Compose에서 backdrop-filter를 완벽하게 구현할 수 없으므로,
 * 반투명 배경 + 테두리 + 그림자로 유사한 효과를 구현합니다.
 */

/**
 * Glass Panel Effect
 * - 가장 기본적인 glass 효과
 * - 네비게이션 바, 작은 패널에 사용
 */
fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp
) = this
    .shadow(elevation = shadowElevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.3f))
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Glass Card Effect
 * - 메인 입력 카드용 glass 효과
 * - 더 진한 배경, 더 강한 테두리
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassCard,
    borderColor: Color = GlassBorderLight,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 12.dp
) = this
    .shadow(elevation = shadowElevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.2f))
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = shape)

/**
 * Glass Button Effect
 * - 버튼용 glass 효과
 * - 가벼운 배경, 얇은 테두리
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
    .then(
        if (shadowElevation > 0.dp) {
            Modifier.shadow(elevation = shadowElevation, shape = shape)
        } else {
            Modifier
        }
    )

/**
 * Glass Button Hover Effect
 * - 버튼 눌림/Hover 상태용
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
