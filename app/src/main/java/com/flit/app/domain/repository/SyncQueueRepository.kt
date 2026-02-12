package com.flit.app.domain.repository

import com.flit.app.domain.model.SyncQueueItem
import com.flit.app.domain.model.SyncQueueStatus

/**
 * 동기화 큐 Repository 인터페이스
 */
interface SyncQueueRepository {

    /** 큐에 작업 추가 */
    suspend fun enqueue(item: SyncQueueItem)

    /** 큐 처리 워커 즉시 트리거 */
    fun triggerProcessing()

    /** PENDING 상태 작업 조회 (next_retry_at 순) */
    suspend fun getPendingItems(): List<SyncQueueItem>

    /** 상태 업데이트 */
    suspend fun updateStatus(itemId: String, status: SyncQueueStatus)

    /** 재시도 횟수 증가 + 다음 재시도 시각 설정 */
    suspend fun incrementRetry(itemId: String, nextRetryAt: Long)

    /** 완료된 작업 삭제 */
    suspend fun deleteCompleted()

    /** PROCESSING 상태를 PENDING으로 리셋 (앱 재시작 시) */
    suspend fun resetProcessingToPending()
}
