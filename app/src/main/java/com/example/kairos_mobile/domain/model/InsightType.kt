package com.example.kairos_mobile.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 인사이트 분류 타입
 */
enum class InsightType {
    IDEA,        // 아이디어 → Obsidian/ideas/
    SCHEDULE,    // 일정 → Google Calendar (Phase 3)
    TODO,        // 할 일 → Todoist (Phase 3)
    NOTE,        // 노트 → Obsidian/inbox/
    QUICK_NOTE;  // 빠른 메모 → Obsidian Daily Note

    /**
     * 각 타입에 해당하는 색상 반환
     */
    fun getColor(): Color {
        return when (this) {
            IDEA -> Color(0xFFFFB74D)      // Amber
            SCHEDULE -> Color(0xFF64B5F6)  // Blue
            TODO -> Color(0xFF81C784)      // Green
            NOTE -> Color(0xFFBA68C8)      // Purple
            QUICK_NOTE -> Color(0xFF90A4AE) // Blue Grey
        }
    }

    /**
     * 사용자 친화적인 이름 반환
     */
    fun getDisplayName(): String {
        return when (this) {
            IDEA -> "아이디어"
            SCHEDULE -> "일정"
            TODO -> "할 일"
            NOTE -> "노트"
            QUICK_NOTE -> "빠른 메모"
        }
    }
}
