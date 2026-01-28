package com.example.kairos_mobile.domain.model

/**
 * 분류 결과의 라우팅 목적지
 */
enum class Destination {
    TODO,       // 앱 내 투두리스트
    OBSIDIAN;   // Obsidian vault

    companion object {
        /**
         * API 응답 값에서 Destination으로 변환
         */
        fun fromApiValue(value: String): Destination {
            return when (value.lowercase()) {
                "todo" -> TODO
                "obsidian" -> OBSIDIAN
                else -> OBSIDIAN  // 기본값
            }
        }

        /**
         * Destination을 API 요청 값으로 변환
         */
        fun toApiValue(destination: Destination): String {
            return when (destination) {
                TODO -> "todo"
                OBSIDIAN -> "obsidian"
            }
        }
    }
}
