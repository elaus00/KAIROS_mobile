package com.example.kairos_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Minimalist Monochrome Modifiers (PRD v4.0)
 *
 * 무채색 기반 미니멀리스트 디자인:
 * - 깔끔한 테두리와 배경
 * - 미묘한 그림자
 * - 단순하고 명확한 계층 구조
 */

// ========== Card Modifiers ==========

/**
 * Kairos Card Modifier
 * - 12px radius
 * - 1px 보더
 * - 미묘한 그림자
 *
 * @param shape 카드 모양 (기본: 12dp radius)
 * @param elevation 그림자 높이 (기본: 2dp)
 */
@Composable
fun Modifier.kairosCard(
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 2.dp
): Modifier {
    val colors = KairosTheme.colors
    val shadowColor = if (colors.isDark) {
        Color.Black.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = shadowColor)
        .clip(shape)
        .background(colors.card)
        .border(width = 1.dp, color = colors.border, shape = shape)
}

/**
 * Kairos Card (테두리 없음)
 * - 배경만 적용
 */
@Composable
fun Modifier.kairosCardNoBorder(
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 2.dp
): Modifier {
    val colors = KairosTheme.colors
    val shadowColor = if (colors.isDark) {
        Color.Black.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = shadowColor)
        .clip(shape)
        .background(colors.card)
}

/**
 * Kairos Elevated Card
 * - 더 높은 그림자
 * - 강조가 필요한 카드용
 */
@Composable
fun Modifier.kairosElevatedCard(
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 6.dp
): Modifier {
    val colors = KairosTheme.colors
    val shadowColor = if (colors.isDark) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color.Black.copy(alpha = 0.12f)
    }

    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = shadowColor)
        .clip(shape)
        .background(colors.card)
        .border(width = 1.dp, color = colors.border, shape = shape)
}

// ========== Chip Modifiers ==========

/**
 * Kairos Chip Modifier
 * - 8dp radius
 * - chip 배경색
 * - AI 분류 chip 등에 사용
 */
@Composable
fun Modifier.kairosChip(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.chipBg)
}

/**
 * Kairos Outlined Chip
 * - 테두리가 있는 칩
 */
@Composable
fun Modifier.kairosOutlinedChip(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(Color.Transparent)
        .border(width = 1.dp, color = colors.border, shape = shape)
}

/**
 * Kairos Selected Chip
 * - 선택된 상태의 칩
 */
@Composable
fun Modifier.kairosSelectedChip(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.accent)
}

// ========== Input Modifiers ==========

/**
 * Kairos Input Modifier
 * - 16dp radius
 * - 입력 필드용
 */
@Composable
fun Modifier.kairosInput(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.accentBg)
        .border(width = 1.dp, color = colors.borderLight, shape = shape)
}

/**
 * Kairos Input Focused
 * - 포커스 상태의 입력 필드
 */
@Composable
fun Modifier.kairosInputFocused(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.card)
        .border(width = 1.dp, color = colors.accent, shape = shape)
}

// ========== Button Modifiers ==========

/**
 * Kairos Primary Button
 * - 채워진 버튼
 * - accent 색상 배경
 */
@Composable
fun Modifier.kairosPrimaryButton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.accent)
}

/**
 * Kairos Secondary Button
 * - 테두리만 있는 버튼
 */
@Composable
fun Modifier.kairosSecondaryButton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(Color.Transparent)
        .border(width = 1.dp, color = colors.border, shape = shape)
}

/**
 * Kairos Ghost Button
 * - 배경/테두리 없는 버튼
 * - 호버/클릭 시에만 배경 표시
 */
@Composable
fun Modifier.kairosGhostButton(
    shape: Shape = RoundedCornerShape(8.dp),
    isPressed: Boolean = false
): Modifier {
    val colors = KairosTheme.colors
    val backgroundColor = if (isPressed) colors.accentBg else Color.Transparent

    return this
        .clip(shape)
        .background(backgroundColor)
}

// ========== Divider / Surface Modifiers ==========

/**
 * Kairos Surface
 * - 기본 표면
 */
@Composable
fun Modifier.kairosSurface(
    shape: Shape = RoundedCornerShape(0.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.background)
}

/**
 * Kairos Accent Surface
 * - 강조 배경
 */
@Composable
fun Modifier.kairosAccentSurface(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.accentBg)
}

// ========== Danger Modifiers ==========

/**
 * Kairos Danger Button
 * - 삭제/위험 액션용
 * - danger 색상 사용
 */
@Composable
fun Modifier.kairosDangerButton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(colors.danger)
}

/**
 * Kairos Danger Outlined Button
 * - 테두리만 있는 위험 버튼
 */
@Composable
fun Modifier.kairosDangerOutlinedButton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val colors = KairosTheme.colors

    return this
        .clip(shape)
        .background(Color.Transparent)
        .border(width = 1.dp, color = colors.danger, shape = shape)
}

// ========== Non-Composable Variants (for interoperability) ==========

/**
 * 색상을 직접 전달하는 Card Modifier
 * Composable이 아닌 곳에서 사용
 */
fun Modifier.kairosCardWithColors(
    backgroundColor: Color,
    borderColor: Color,
    isDark: Boolean,
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 2.dp
): Modifier {
    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = shadowColor)
        .clip(shape)
        .background(backgroundColor)
        .border(width = 1.dp, color = borderColor, shape = shape)
}

/**
 * 색상을 직접 전달하는 Chip Modifier
 */
fun Modifier.kairosChipWithColors(
    backgroundColor: Color,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    return this
        .clip(shape)
        .background(backgroundColor)
}
