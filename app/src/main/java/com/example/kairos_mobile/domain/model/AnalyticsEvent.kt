package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 분석 이벤트 도메인 모델
 */
data class AnalyticsEvent(
    val id: String = UUID.randomUUID().toString(),
    /** 이벤트 유형 */
    val eventType: String,
    /** 이벤트 데이터 (JSON) */
    val eventData: String? = null,
    /** 이벤트 발생 시각 */
    val timestamp: Long = System.currentTimeMillis(),
    /** 서버 동기화 여부 */
    val isSynced: Boolean = false
)
