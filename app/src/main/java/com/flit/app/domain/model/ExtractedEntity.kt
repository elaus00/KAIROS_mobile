package com.flit.app.domain.model

import java.util.UUID

/**
 * AI 추출 엔티티 도메인 모델
 * 캡처 텍스트에서 AI가 추출한 핵심 개체
 */
data class ExtractedEntity(
    val id: String = UUID.randomUUID().toString(),
    /** FK → Capture.id */
    val captureId: String,
    /** 엔티티 유형 */
    val type: EntityType,
    /** 원문 표현 (예: "금요일", "강남역") */
    val value: String,
    /** 정규화된 값 (예: "2026-02-13", "강남역") */
    val normalizedValue: String? = null
)
