package com.example.kairos_mobile.domain.model

/**
 * 동기화 큐 아이템 상태
 */
enum class SyncQueueStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
