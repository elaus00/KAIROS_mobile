package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 동기화 큐 Room Entity
 * 오프라인 시 서버 요청을 큐잉.
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["next_retry_at"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    val id: String,

    // 작업 유형: CLASSIFY, RECLASSIFY, CALENDAR_CREATE, CALENDAR_DELETE, ANALYTICS_BATCH
    val action: String,

    // JSON 직렬화 요청 데이터
    val payload: String,

    // 재시도 횟수
    @ColumnInfo(name = "retry_count", defaultValue = "0")
    val retryCount: Int = 0,

    // 최대 재시도 횟수
    @ColumnInfo(name = "max_retries", defaultValue = "3")
    val maxRetries: Int = 3,

    // 상태: PENDING, PROCESSING, COMPLETED, FAILED
    val status: String,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 다음 재시도 예정 시각
    @ColumnInfo(name = "next_retry_at")
    val nextRetryAt: Long? = null
)
