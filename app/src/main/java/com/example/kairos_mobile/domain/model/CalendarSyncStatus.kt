package com.example.kairos_mobile.domain.model

/**
 * 캘린더 동기화 상태
 */
enum class CalendarSyncStatus {
    /** 연결되지 않음 */
    NOT_LINKED,
    /** 제안 대기 (사용자 승인 필요) */
    SUGGESTION_PENDING,
    /** 동기화 완료 */
    SYNCED,
    /** 동기화 실패 */
    SYNC_FAILED,
    /** 사용자가 제안 거부 */
    REJECTED
}
