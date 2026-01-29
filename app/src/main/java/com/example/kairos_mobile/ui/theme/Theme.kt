package com.example.kairos_mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ========== Kairos Colors (PRD v4.0) ==========

/**
 * Kairos 앱 전용 색상 클래스
 * Material3 ColorScheme과 별도로 관리
 */
@Immutable
data class KairosColors(
    val background: Color,
    val card: Color,
    val border: Color,
    val borderLight: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val placeholder: Color,
    val accent: Color,
    val accentBg: Color,
    val chipBg: Color,
    val chipText: Color,
    val danger: Color,
    val success: Color,
    val icon: Color,
    val iconMuted: Color,
    val divider: Color,
    val isDark: Boolean
)

/**
 * 라이트 테마 색상
 */
val LightKairosColors = KairosColors(
    background = KairosLight.background,
    card = KairosLight.card,
    border = KairosLight.border,
    borderLight = KairosLight.borderLight,
    text = KairosLight.text,
    textSecondary = KairosLight.textSecondary,
    textMuted = KairosLight.textMuted,
    placeholder = KairosLight.placeholder,
    accent = KairosLight.accent,
    accentBg = KairosLight.accentBg,
    chipBg = KairosLight.chipBg,
    chipText = KairosLight.chipText,
    danger = KairosLight.danger,
    success = KairosLight.success,
    icon = KairosLight.icon,
    iconMuted = KairosLight.iconMuted,
    divider = KairosLight.divider,
    isDark = false
)

/**
 * 다크 테마 색상
 */
val DarkKairosColors = KairosColors(
    background = KairosDark.background,
    card = KairosDark.card,
    border = KairosDark.border,
    borderLight = KairosDark.borderLight,
    text = KairosDark.text,
    textSecondary = KairosDark.textSecondary,
    textMuted = KairosDark.textMuted,
    placeholder = KairosDark.placeholder,
    accent = KairosDark.accent,
    accentBg = KairosDark.accentBg,
    chipBg = KairosDark.chipBg,
    chipText = KairosDark.chipText,
    danger = KairosDark.danger,
    success = KairosDark.success,
    icon = KairosDark.icon,
    iconMuted = KairosDark.iconMuted,
    divider = KairosDark.divider,
    isDark = true
)

/**
 * CompositionLocal for KairosColors
 */
val LocalKairosColors = staticCompositionLocalOf { LightKairosColors }

// ========== Material3 Color Schemes ==========

/**
 * Minimalist Light Color Scheme
 */
private val MinimalistLightColorScheme = lightColorScheme(
    primary = KairosLight.accent,
    onPrimary = Color.White,
    primaryContainer = KairosLight.accentBg,
    onPrimaryContainer = KairosLight.text,

    secondary = KairosLight.chipBg,
    onSecondary = KairosLight.chipText,
    secondaryContainer = KairosLight.accentBg,
    onSecondaryContainer = KairosLight.textSecondary,

    tertiary = KairosLight.textSecondary,
    onTertiary = Color.White,

    background = KairosLight.background,
    onBackground = KairosLight.text,

    surface = KairosLight.card,
    onSurface = KairosLight.text,
    surfaceVariant = KairosLight.accentBg,
    onSurfaceVariant = KairosLight.textSecondary,

    error = KairosLight.danger,
    onError = Color.White,

    outline = KairosLight.border,
    outlineVariant = KairosLight.borderLight
)

/**
 * Minimalist Dark Color Scheme
 */
private val MinimalistDarkColorScheme = darkColorScheme(
    primary = KairosDark.accent,
    onPrimary = KairosDark.background,
    primaryContainer = KairosDark.accentBg,
    onPrimaryContainer = KairosDark.text,

    secondary = KairosDark.chipBg,
    onSecondary = KairosDark.chipText,
    secondaryContainer = KairosDark.accentBg,
    onSecondaryContainer = KairosDark.textSecondary,

    tertiary = KairosDark.textSecondary,
    onTertiary = KairosDark.background,

    background = KairosDark.background,
    onBackground = KairosDark.text,

    surface = KairosDark.card,
    onSurface = KairosDark.text,
    surfaceVariant = KairosDark.accentBg,
    onSurfaceVariant = KairosDark.textSecondary,

    error = KairosDark.danger,
    onError = Color.White,

    outline = KairosDark.border,
    outlineVariant = KairosDark.borderLight
)

// ========== Theme Composable ==========

/**
 * KAIROS Theme (PRD v4.0)
 * Minimalist Monochrome 디자인 적용
 *
 * @param darkTheme 다크 모드 여부 (기본: 시스템 설정 따름)
 * @param content 콘텐츠
 */
@Composable
fun KAIROS_mobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 색상 스킴 선택
    val colorScheme = when {
        darkTheme -> MinimalistDarkColorScheme
        else -> MinimalistLightColorScheme
    }

    // Kairos 전용 색상
    val kairosColors = if (darkTheme) DarkKairosColors else LightKairosColors

    CompositionLocalProvider(
        LocalKairosColors provides kairosColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Kairos 색상에 쉽게 접근하기 위한 object
 */
object KairosTheme {
    /**
     * 현재 테마의 Kairos 색상
     */
    val colors: KairosColors
        @Composable
        get() = LocalKairosColors.current
}
