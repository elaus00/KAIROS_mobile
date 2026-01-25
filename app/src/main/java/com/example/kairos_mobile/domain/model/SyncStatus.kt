package com.example.kairos_mobile.domain.model

/**
 * 캡처의 동기화 상태
 */
enum class SyncStatus {
    PENDING,   // 대기중 (오프라인 또는 미처리)
    SYNCING,   // 동기화 중
    SYNCED,    // 동기화 완료
    FAILED     // 동기화 실패
}
