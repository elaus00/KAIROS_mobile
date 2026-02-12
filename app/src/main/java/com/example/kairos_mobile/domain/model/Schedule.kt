package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 일정 도메인 모델
 * Capture가 SCHEDULE로 분류될 때 생성되는 파생 엔티티
 */
data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    /** FK → Capture.id (1:1) */
    val captureId: String,
    /** 일정 제목 (Capture 텍스트에서 파생) */
    val title: String = "",
    /** 시작 일시 (epoch ms) */
    val startTime: Long? = null,
    /** 종료 일시 (epoch ms) */
    val endTime: Long? = null,
    /** 장소 */
    val location: String? = null,
    /** 종일 이벤트 여부 */
    val isAllDay: Boolean = false,
    /** 일정 신뢰도 */
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM,
    /** 기기 캘린더 동기화 상태 */
    val calendarSyncStatus: CalendarSyncStatus = CalendarSyncStatus.NOT_LINKED,
    /** 캘린더 이벤트 ID (DB column: google_event_id) */
    val calendarEventId: String? = null,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 최종 수정 시각 */
    val updatedAt: Long = createdAt
)
