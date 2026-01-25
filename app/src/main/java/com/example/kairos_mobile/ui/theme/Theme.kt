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
private val GlassmorphismColorScheme = darkColorScheme(
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
 * KAIROS Glassmorphism Theme
 * 항상 다크 모드 + Glassmorphism 스타일 사용
 */
@Composable
fun KAIROS_mobileTheme(
    content: @Composable () -> Unit
) {
    // Glassmorphism은 항상 다크 모드 기반
    val colorScheme = GlassmorphismColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}