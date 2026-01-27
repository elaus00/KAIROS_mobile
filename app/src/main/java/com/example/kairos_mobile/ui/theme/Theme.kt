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
import androidx.compose.ui.graphics.Color
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
 * Glassmorphism Light Color Scheme - Airy Design
 * 밝고 가벼운 에어리 디자인 (파스텔 그라데이션 기반)
 */
private val GlassmorphismLightColorScheme = lightColorScheme(
    primary = AiryAccentBlue,
    onPrimary = Color.White,
    primaryContainer = AiryAccentBlueLight,
    onPrimaryContainer = AiryTextPrimary,

    secondary = AiryGlassCard,
    onSecondary = AiryTextSecondary,
    secondaryContainer = AiryGlassPanel,
    onSecondaryContainer = AiryTextSecondary,

    tertiary = AiryIdeaColor,
    onTertiary = Color.White,

    background = AiryGradientStart,
    onBackground = AiryTextPrimary,

    surface = AiryGlassCard,
    onSurface = AiryTextPrimary,
    surfaceVariant = AiryGlassPanel,
    onSurfaceVariant = AiryTextSecondary,

    error = AiryErrorColor,
    onError = Color.White,

    outline = AiryGlassBorder,
    outlineVariant = AiryGlassBorderStrong
)

/**
 * KAIROS Glassmorphism Theme
 * 다크/라이트 모드 전환 지원 + Glassmorphism 스타일 사용
 */
@Composable
fun KAIROS_mobileTheme(
    darkTheme: Boolean = false,  // 기본값을 라이트 테마로 변경 (에어리 디자인)
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