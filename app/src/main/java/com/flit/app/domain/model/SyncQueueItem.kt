package com.flit.app.domain.model

import java.util.UUID

/**
 * 동기화 큐 아이템 도메인 모델
 * 오프라인 시 서버 요청을 큐잉
 */
data class SyncQueueItem(
    val id: String = UUID.randomUUID().toString(),
    /** 작업 유형 */
    val action: SyncAction,
    /** JSON 직렬화 요청 데이터 */
    val payload: String,
    /** 재시도 횟수 */
    val retryCount: Int = 0,
    /** 최대 재시도 횟수 */
    val maxRetries: Int = 3,
    /** 큐 상태 */
    val status: SyncQueueStatus = SyncQueueStatus.PENDING,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 다음 재시도 예정 시각 */
    val nextRetryAt: Long? = null
)
