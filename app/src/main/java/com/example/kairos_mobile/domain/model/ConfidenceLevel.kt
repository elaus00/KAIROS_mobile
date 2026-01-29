package com.example.kairos_mobile.domain.model

/**
 * AI 분류 신뢰도 레벨
 * ResultScreen에서 UI 분기에 사용
 */
enum class ConfidenceLevel {
    HIGH,    // ≥95% - 자동저장 모드
    MEDIUM,  // 80~95% - 확인 모드
    LOW;     // <80% - 선택 모드

    companion object {
        /**
         * 신뢰도 값(0.0~1.0)으로 레벨 결정
         */
        fun fromConfidence(confidence: Float): ConfidenceLevel {
            return when {
                confidence >= 0.95f -> HIGH
                confidence >= 0.80f -> MEDIUM
                else -> LOW
            }
        }

        /**
         * 백분율(0~100)로 레벨 결정
         */
        fun fromPercentage(percentage: Int): ConfidenceLevel {
            return fromConfidence(percentage / 100f)
        }
    }
}
