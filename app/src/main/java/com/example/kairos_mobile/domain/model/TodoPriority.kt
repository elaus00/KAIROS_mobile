package com.example.kairos_mobile.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 투두 우선순위
 */
enum class TodoPriority(val value: Int) {
    NONE(0),      // 우선순위 없음
    LOW(1),       // 낮음
    MEDIUM(2),    // 중간
    HIGH(3);      // 높음

    /**
     * 우선순위에 해당하는 색상 반환
     */
    fun getColor(): Color {
        return when (this) {
            NONE -> Color(0xFF90A4AE)      // Blue Grey
            LOW -> Color(0xFF81C784)        // Green
            MEDIUM -> Color(0xFFFFB74D)     // Amber
            HIGH -> Color(0xFFE57373)       // Red
        }
    }

    /**
     * 사용자 친화적인 이름 반환
     */
    fun getDisplayName(): String {
        return when (this) {
            NONE -> "없음"
            LOW -> "낮음"
            MEDIUM -> "중간"
            HIGH -> "높음"
        }
    }

    companion object {
        fun fromValue(value: Int): TodoPriority {
            return entries.find { it.value == value } ?: NONE
        }
    }
}
