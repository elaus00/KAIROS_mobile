package com.example.kairos_mobile.ui.theme

import androidx.compose.ui.graphics.Color

// ========== Glassmorphism Color Palette ==========

// Primary Colors (Navy)
val PrimaryNavy = Color(0xFF1A237E)          // 메인 네이비
val PrimaryNavyLight = Color(0xFF534BAE)     // 밝은 네이비
val PrimaryNavyHover = Color(0xFF252F9C)     // Hover 상태

// Background Colors (무채색 + 네이비)
val NavyDark = Color(0xFF0A0E17)             // 메인 배경
val Charcoal = Color(0xFF121212)             // 어두운 차콜

// Glass Effect Colors (반투명)
val GlassSurface = Color(0x08FFFFFF)         // rgba(255,255,255,0.03)
val GlassCard = Color(0x4D141E1E)            // rgba(20,20,30,0.3)
val GlassButton = Color(0x0DFFFFFF)          // rgba(255,255,255,0.05)
val GlassButtonHover = Color(0x14FFFFFF)     // rgba(255,255,255,0.08)

// Border Colors (반투명)
val GlassBorder = Color(0x26FFFFFF)          // rgba(255,255,255,0.15)
val GlassBorderLight = Color(0x33FFFFFF)     // rgba(255,255,255,0.2)
val GlassBorderDim = Color(0x1AFFFFFF)       // rgba(255,255,255,0.1)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)          // 기본 텍스트
val TextSecondary = Color(0xE6FFFFFF)        // rgba(255,255,255,0.9)
val TextTertiary = Color(0x99FFFFFF)         // rgba(255,255,255,0.6)
val TextQuaternary = Color(0x66FFFFFF)       // rgba(255,255,255,0.4)
val TextDim = Color(0xB3FFFFFF)              // rgba(255,255,255,0.7)
val TextMuted = Color(0xCCFFFFFF)            // rgba(255,255,255,0.8)

// Blob Colors (애니메이션 배경)
val BlobNavy = Color(0x331A237E)             // rgba(26,35,126,0.2)
val BlobSlate = Color(0x1A475569)            // rgba(71,85,105,0.1)
val BlobNavyDim = Color(0x1A1A237E)          // rgba(26,35,126,0.1)

// Utility Colors
val ErrorColor = Color(0xFFD32F2F)
val SuccessColor = Color(0xFF388E3C)
val WarningColor = Color(0xFFFFA726)

// Semantic Colors (다크 테마)
val ErrorRed = Color(0xFFE57373)            // 에러/위험 (밝은 레드)
val SuccessGreen = Color(0xFF81C784)        // 성공 (밝은 그린)
val WarningOrange = Color(0xFFFFB74D)       // 경고 (오렌지)
val GlassBackground = Color(0xFF0A0E17)     // 다크 배경

// Category Colors (Quick Type Buttons)
val IdeaColor = Color(0xFFFFB74D)            // Amber
val MeetingColor = Color(0xFF64B5F6)         // Blue
val TodoColor = Color(0xFF81C784)            // Green
val SaveColor = Color(0xFFBA68C8)            // Purple

// Accent Colors
val AccentBlue = Color(0xFF64B5F6)           // Accent Blue

// ========== Light Theme Colors ==========

// Light Background Colors
val LightBackground = Color(0xFFF5F7FA)      // 메인 배경
val LightSurface = Color(0xFFFFFFFF)         // 서피스

// Light Glass Effect Colors
val LightGlassSurface = Color(0x08000000)    // rgba(0,0,0,0.03)
val LightGlassCard = Color(0x1AFFFFFF)       // rgba(255,255,255,0.1)
val LightGlassButton = Color(0x0D000000)     // rgba(0,0,0,0.05)
val LightGlassButtonHover = Color(0x14000000) // rgba(0,0,0,0.08)

// Light Border Colors
val LightGlassBorder = Color(0x26000000)     // rgba(0,0,0,0.15)
val LightGlassBorderLight = Color(0x33000000) // rgba(0,0,0,0.2)
val LightGlassBorderDim = Color(0x1A000000)  // rgba(0,0,0,0.1)

// Light Text Colors
val LightTextPrimary = Color(0xFF1A1A1A)     // 기본 텍스트
val LightTextSecondary = Color(0xE61A1A1A)   // rgba(26,26,26,0.9)
val LightTextTertiary = Color(0x991A1A1A)    // rgba(26,26,26,0.6)
val LightTextQuaternary = Color(0x661A1A1A)  // rgba(26,26,26,0.4)
val LightTextDim = Color(0xB31A1A1A)         // rgba(26,26,26,0.7)
val LightTextMuted = Color(0xCC1A1A1A)       // rgba(26,26,26,0.8)

// Light Blob Colors
val LightBlobNavy = Color(0x1A1A237E)        // rgba(26,35,126,0.1)
val LightBlobSlate = Color(0x0D475569)       // rgba(71,85,105,0.05)
val LightBlobNavyDim = Color(0x0D1A237E)     // rgba(26,35,126,0.05)

// ========== Airy Design (새로운 밝은 테마) ==========

// 그라데이션 배경 색상
val AiryGradientStart = Color(0xFF94B9E5)    // 파스텔 블루
val AiryGradientEnd = Color(0xFFFBD1E8)      // 파스텔 핑크

// 에어리 글래스 효과 (높은 불투명도)
val AiryGlassCard = Color(0x73FFFFFF)        // 45% 흰색
val AiryGlassBorder = Color(0x80FFFFFF)      // 50% 흰색
val AiryGlassBorderStrong = Color(0x99FFFFFF) // 60% 흰색
val AiryGlassPanel = Color(0x66FFFFFF)       // 40% 흰색

// 에어리 텍스트 색상 (슬레이트 계열)
val AiryTextPrimary = Color(0xFF1E293B)      // 슬레이트 900
val AiryTextSecondary = Color(0xFF334155)    // 슬레이트 700
val AiryTextTertiary = Color(0xFF64748B)     // 슬레이트 500
val AiryTextQuaternary = Color(0xFF94A3B8)   // 슬레이트 400

// 에어리 파스텔 Blob
val AiryBlobBlue = Color(0x40A5C8F0)         // 파스텔 블루 25%
val AiryBlobPink = Color(0x40F8C8E0)         // 파스텔 핑크 25%
val AiryBlobPurple = Color(0x40D4B8F0)       // 파스텔 퍼플 25%

// 에어리 디자인 v2 (SVG 스펙 기반)
val AiryBackgroundV2 = Color(0xFFF4F6F8)     // 밝은 회색 배경
val AiryBlobNavyDark = Color(0x261A1A40)     // 상단 Blob - 어두운 남색 15%
val AiryBlobSkyBlue = Color(0x33BFDBFE)      // 하단 Blob - 연한 파란색 20%

// 에어리 액센트 색상
val AiryAccentBlue = Color(0xFF2A86E2)       // 밝은 블루
val AiryAccentBlueLight = Color(0x262A86E2)  // 15% 불투명도

// 에어리 카테고리 색상 (파스텔)
val AiryIdeaColor = Color(0xFFF59E0B)        // 황색
val AiryMeetingColor = Color(0xFF3B82F6)     // 블루
val AiryTodoColor = Color(0xFF10B981)        // 그린
val AirySaveColor = Color(0xFFA855F7)        // 퍼플

// 에어리 유틸리티 색상
val AirySuccessColor = Color(0xFF22C55E)     // 그린
val AiryWarningColor = Color(0xFFFBBF24)     // 노란색
val AiryErrorColor = Color(0xFFEF4444)       // 레드

// 에어리 Semantic Colors (라이트 테마)
val AiryErrorRed = Color(0xFFEF4444)         // 에러/위험 (레드)
val AirySuccessGreen = Color(0xFF22C55E)     // 성공 (그린)
val AiryWarningOrange = Color(0xFFF97316)    // 경고 (오렌지)
val AiryGlassBackground = Color(0xFFF5F7FA)  // 라이트 배경

// ========== Minimalist Monochrome Design (PRD v4.0) ==========

/**
 * 라이트 테마 색상 팔레트
 * 무채색 기조, danger만 예외
 */
object KairosLight {
    val background = Color(0xFFFAFAFA)       // 메인 배경
    val card = Color(0xFFFFFFFF)             // 카드 배경
    val border = Color(0xFFEEEEEE)           // 기본 보더
    val borderLight = Color(0xFFF0F0F0)      // 연한 보더
    val text = Color(0xFF111111)             // 기본 텍스트
    val textSecondary = Color(0xFF888888)    // 보조 텍스트
    val textMuted = Color(0xFFAAAAAA)        // 희미한 텍스트
    val placeholder = Color(0xFFCCCCCC)      // 플레이스홀더
    val accent = Color(0xFF111111)           // 강조 색상
    val accentBg = Color(0xFFF5F5F5)         // 강조 배경
    val chipBg = Color(0xFFF0F0F0)           // 칩 배경
    val chipText = Color(0xFF666666)         // 칩 텍스트
    val danger = Color(0xFFEF4444)           // 위험/삭제 (유일한 컬러)
    val success = Color(0xFF10B981)          // 성공/연동됨 (녹색)
    val icon = Color(0xFF555555)             // 아이콘 색상
    val iconMuted = Color(0xFFAAAAAA)        // 비활성 아이콘
    val divider = Color(0xFFF0F0F0)          // 구분선
}

/**
 * 다크 테마 색상 팔레트
 * 무채색 기조, danger만 예외
 */
object KairosDark {
    val background = Color(0xFF0A0A0A)       // 메인 배경
    val card = Color(0xFF1A1A1A)             // 카드 배경
    val border = Color(0xFF2A2A2A)           // 기본 보더
    val borderLight = Color(0xFF222222)      // 연한 보더
    val text = Color(0xCCFFFFFF)             // 기본 텍스트 (80% white)
    val textSecondary = Color(0xFF888888)    // 보조 텍스트
    val textMuted = Color(0xFF555555)        // 희미한 텍스트
    val placeholder = Color(0xFF444444)      // 플레이스홀더
    val accent = Color(0xCCFFFFFF)           // 강조 색상
    val accentBg = Color(0xFF252525)         // 강조 배경
    val chipBg = Color(0xFF2A2A2A)           // 칩 배경
    val chipText = Color(0xFF999999)         // 칩 텍스트
    val danger = Color(0xFFEF4444)           // 위험/삭제 (유일한 컬러)
    val success = Color(0xFF10B981)          // 성공/연동됨 (녹색)
    val icon = Color(0xFFAAAAAA)             // 아이콘 색상
    val iconMuted = Color(0xFF555555)        // 비활성 아이콘
    val divider = Color(0xFF2A2A2A)          // 구분선
}