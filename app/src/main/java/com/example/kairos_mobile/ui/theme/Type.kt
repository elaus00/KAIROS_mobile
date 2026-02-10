package com.example.kairos_mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 글쓰기 전용 세리프 폰트 (캡처 입력 영역)
 * 대부분의 한국어 Android 기기에서 Noto Serif CJK KR로 렌더링됨
 * UI 폰트(고딕)와 분리하여 "글을 쓰는 느낌"을 부여
 */
val KairosWritingFontFamily = FontFamily.Serif

// Kairos 앱 타이포그래피 정의 (UI용 — 기본 시스템 고딕)
val KairosTypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
)
