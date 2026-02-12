package com.flit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ========== Flit Colors (PRD v4.0) ==========

/**
 * Flit 앱 전용 색상 클래스
 * Material3 ColorScheme과 별도로 관리
 */
@Immutable
data class FlitColors(
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
    val warning: Color,
    val icon: Color,
    val iconMuted: Color,
    val divider: Color,
    val isDark: Boolean
)

/**
 * 라이트 테마 색상
 */
val LightFlitColors = FlitColors(
    background = FlitLight.background,
    card = FlitLight.card,
    border = FlitLight.border,
    borderLight = FlitLight.borderLight,
    text = FlitLight.text,
    textSecondary = FlitLight.textSecondary,
    textMuted = FlitLight.textMuted,
    placeholder = FlitLight.placeholder,
    accent = FlitLight.accent,
    accentBg = FlitLight.accentBg,
    chipBg = FlitLight.chipBg,
    chipText = FlitLight.chipText,
    danger = FlitLight.danger,
    success = FlitLight.success,
    warning = FlitLight.warning,
    icon = FlitLight.icon,
    iconMuted = FlitLight.iconMuted,
    divider = FlitLight.divider,
    isDark = false
)

/**
 * 다크 테마 색상
 */
val DarkFlitColors = FlitColors(
    background = FlitDark.background,
    card = FlitDark.card,
    border = FlitDark.border,
    borderLight = FlitDark.borderLight,
    text = FlitDark.text,
    textSecondary = FlitDark.textSecondary,
    textMuted = FlitDark.textMuted,
    placeholder = FlitDark.placeholder,
    accent = FlitDark.accent,
    accentBg = FlitDark.accentBg,
    chipBg = FlitDark.chipBg,
    chipText = FlitDark.chipText,
    danger = FlitDark.danger,
    success = FlitDark.success,
    warning = FlitDark.warning,
    icon = FlitDark.icon,
    iconMuted = FlitDark.iconMuted,
    divider = FlitDark.divider,
    isDark = true
)

/**
 * CompositionLocal for FlitColors
 */
val LocalFlitColors = staticCompositionLocalOf { LightFlitColors }

// ========== Material3 Color Schemes ==========

/**
 * Minimalist Light Color Scheme
 */
private val MinimalistLightColorScheme = lightColorScheme(
    primary = FlitLight.accent,
    onPrimary = Color.White,
    primaryContainer = FlitLight.accentBg,
    onPrimaryContainer = FlitLight.text,

    secondary = FlitLight.chipBg,
    onSecondary = FlitLight.chipText,
    secondaryContainer = FlitLight.accentBg,
    onSecondaryContainer = FlitLight.textSecondary,

    tertiary = FlitLight.textSecondary,
    onTertiary = Color.White,

    background = FlitLight.background,
    onBackground = FlitLight.text,

    surface = FlitLight.card,
    onSurface = FlitLight.text,
    surfaceVariant = FlitLight.accentBg,
    onSurfaceVariant = FlitLight.textSecondary,

    error = FlitLight.danger,
    onError = Color.White,

    outline = FlitLight.border,
    outlineVariant = FlitLight.borderLight
)

/**
 * Minimalist Dark Color Scheme
 */
private val MinimalistDarkColorScheme = darkColorScheme(
    primary = FlitDark.accent,
    onPrimary = FlitDark.background,
    primaryContainer = FlitDark.accentBg,
    onPrimaryContainer = FlitDark.text,

    secondary = FlitDark.chipBg,
    onSecondary = FlitDark.chipText,
    secondaryContainer = FlitDark.accentBg,
    onSecondaryContainer = FlitDark.textSecondary,

    tertiary = FlitDark.textSecondary,
    onTertiary = FlitDark.background,

    background = FlitDark.background,
    onBackground = FlitDark.text,

    surface = FlitDark.card,
    onSurface = FlitDark.text,
    surfaceVariant = FlitDark.accentBg,
    onSurfaceVariant = FlitDark.textSecondary,

    error = FlitDark.danger,
    onError = Color.White,

    outline = FlitDark.border,
    outlineVariant = FlitDark.borderLight
)

// ========== Theme Composable ==========

/**
 * Flit Theme (PRD v4.0)
 * Minimalist Monochrome 디자인 적용
 *
 * @param darkTheme 다크 모드 여부 (기본: 시스템 설정 따름)
 * @param content 콘텐츠
 */
@Composable
fun FlitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 색상 스킴 선택
    val colorScheme = when {
        darkTheme -> MinimalistDarkColorScheme
        else -> MinimalistLightColorScheme
    }

    // Flit 전용 색상
    val flitColors = if (darkTheme) DarkFlitColors else LightFlitColors

    CompositionLocalProvider(
        LocalFlitColors provides flitColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FlitTypography,
            content = content
        )
    }
}

/**
 * Flit 색상에 쉽게 접근하기 위한 object
 */
object FlitTheme {
    /**
     * 현재 테마의 Flit 색상
     */
    val colors: FlitColors
        @Composable
        get() = LocalFlitColors.current
}
