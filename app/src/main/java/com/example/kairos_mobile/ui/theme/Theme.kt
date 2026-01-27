package com.example.kairos_mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Glassmorphism Dark Color Scheme
 * 네이비 포인트 + 무채색 기반
 */
private val GlassmorphismDarkColorScheme = darkColorScheme(
    primary = PrimaryNavy,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryNavyLight,
    onPrimaryContainer = TextPrimary,

    secondary = GlassButton,
    onSecondary = TextSecondary,
    secondaryContainer = GlassButtonHover,
    onSecondaryContainer = TextSecondary,

    tertiary = IdeaColor,
    onTertiary = Charcoal,

    background = NavyDark,
    onBackground = TextPrimary,

    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassCard,
    onSurfaceVariant = TextSecondary,

    error = ErrorColor,
    onError = TextPrimary,

    outline = GlassBorder,
    outlineVariant = GlassBorderDim
)

/**
 * Glassmorphism Light Color Scheme
 * 네이비 포인트 + 밝은 배경 기반
 */
private val GlassmorphismLightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = LightTextPrimary,
    primaryContainer = PrimaryNavyLight,
    onPrimaryContainer = LightTextPrimary,

    secondary = LightGlassButton,
    onSecondary = LightTextSecondary,
    secondaryContainer = LightGlassButtonHover,
    onSecondaryContainer = LightTextSecondary,

    tertiary = IdeaColor,
    onTertiary = LightBackground,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightGlassSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassCard,
    onSurfaceVariant = LightTextSecondary,

    error = ErrorColor,
    onError = TextPrimary,

    outline = LightGlassBorder,
    outlineVariant = LightGlassBorderDim
)

/**
 * KAIROS Glassmorphism Theme
 * 다크/라이트 모드 전환 지원 + Glassmorphism 스타일 사용
 */
@Composable
fun KAIROS_mobileTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // 테마에 따라 색상 스킴 선택
    val colorScheme = if (darkTheme) {
        GlassmorphismDarkColorScheme
    } else {
        GlassmorphismLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}