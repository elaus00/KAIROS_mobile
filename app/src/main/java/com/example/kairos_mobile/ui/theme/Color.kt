package com.example.kairos_mobile.ui.theme

import androidx.compose.ui.graphics.Color

// ========== Minimalist Monochrome Design (PRD v4.0) ==========

/**
 * 라이트 테마 색상 팔레트
 * 무채색 기조, danger/success만 예외
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
    val warning = Color(0xFFF59E0B)          // 경고 (황색)
    val icon = Color(0xFF555555)             // 아이콘 색상
    val iconMuted = Color(0xFFAAAAAA)        // 비활성 아이콘
    val divider = Color(0xFFF0F0F0)          // 구분선
}

/**
 * 다크 테마 색상 팔레트
 * 무채색 기조, danger/success만 예외
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
    val warning = Color(0xFFF59E0B)          // 경고 (황색)
    val icon = Color(0xFFAAAAAA)             // 아이콘 색상
    val iconMuted = Color(0xFF555555)        // 비활성 아이콘
    val divider = Color(0xFF2A2A2A)          // 구분선
}
