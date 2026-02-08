package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.SyncQueueEntity

/**
 * 동기화 큐 DAO
 */
@Dao
interface SyncQueueDao {

    /**
     * 큐 항목 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    /**
     * PENDING 상태 항목 조회 (재시도 시각 도래한 항목)
     */
    @Query("""
        SELECT * FROM sync_queue
        WHERE status = 'PENDING'
        AND (next_retry_at IS NULL OR next_retry_at <= :currentTime)
        ORDER BY created_at ASC
    """)
    suspend fun getPendingItems(currentTime: Long): List<SyncQueueEntity>

    /**
     * 상태 업데이트
     */
    @Query("""
        UPDATE sync_queue
        SET status = :status
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: String)

    /**
     * 재시도 정보 업데이트
     */
    @Query("""
        UPDATE sync_queue
        SET retry_count = retry_count + 1,
            next_retry_at = :nextRetryAt,
            status = 'PENDING'
        WHERE id = :id
    """)
    suspend fun incrementRetry(id: String, nextRetryAt: Long)

    /**
     * 완료 항목 삭제
     */
    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    /**
     * PROCESSING 상태 → PENDING으로 리셋 (앱 재시작 시)
     */
    @Query("UPDATE sync_queue SET status = 'PENDING' WHERE status = 'PROCESSING'")
    suspend fun resetProcessingToPending()

}
