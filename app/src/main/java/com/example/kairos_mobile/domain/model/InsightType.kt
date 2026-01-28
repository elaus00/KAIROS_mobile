package com.example.kairos_mobile.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 인사이트 분류 타입
 * API v2.1 기준: idea, todo, note, quick_note, clip
 */
enum class InsightType {
    IDEA,        // 아이디어 → Obsidian/ideas/
    TODO,        // 할 일 → 앱 내 투두리스트
    NOTE,        // 노트 → Obsidian/inbox/
    QUICK_NOTE,  // 빠른 메모 → Obsidian Daily Note
    CLIP,        // 웹 클립 → Obsidian/clips/

    @Deprecated("SCHEDULE은 NOTE로 마이그레이션됨. DB 호환성을 위해 유지")
    SCHEDULE;    // 일정 → NOTE로 변환됨

    /**
     * 각 타입에 해당하는 색상 반환
     */
    fun getColor(): Color {
        return when (this) {
            IDEA -> Color(0xFFFFB74D)       // Amber
            SCHEDULE -> Color(0xFF64B5F6)   // Blue
            TODO -> Color(0xFF81C784)       // Green
            NOTE -> Color(0xFFBA68C8)       // Purple
            QUICK_NOTE -> Color(0xFF90A4AE) // Blue Grey
            CLIP -> Color(0xFF4FC3F7)       // Light Blue
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
            CLIP -> "웹 클립"
        }
    }

    companion object {
        /**
         * API 응답 값에서 InsightType으로 변환
         */
        fun fromApiValue(value: String): InsightType {
            return when (value.lowercase()) {
                "idea" -> IDEA
                "todo" -> TODO
                "note" -> NOTE
                "quick_note" -> QUICK_NOTE
                "clip" -> CLIP
                "schedule" -> SCHEDULE
                else -> NOTE  // 기본값
            }
        }

        /**
         * InsightType을 API 요청 값으로 변환
         */
        fun toApiValue(type: InsightType): String {
            return when (type) {
                IDEA -> "idea"
                TODO -> "todo"
                NOTE -> "note"
                QUICK_NOTE -> "quick_note"
                CLIP -> "clip"
                SCHEDULE -> "note"  // deprecated, note로 변환
            }
        }
    }
}
