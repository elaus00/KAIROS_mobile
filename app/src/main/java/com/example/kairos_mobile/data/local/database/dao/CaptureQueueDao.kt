package com.example.kairos_mobile.data.local.database.dao

import androidx.room.*
import com.example.kairos_mobile.data.local.database.entities.CaptureQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * 캡처 큐 Data Access Object
 */
@Dao
interface CaptureQueueDao {

    /**
     * 특정 상태의 캡처 항목들 조회
     */
    @Query("SELECT * FROM capture_queue WHERE sync_status = :status ORDER BY timestamp DESC")
    fun getCapturesByStatus(status: String): Flow<List<CaptureQueueEntity>>

    /**
     * 최근 캡처 항목들 조회
     */
    @Query("SELECT * FROM capture_queue ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCaptures(limit: Int = 50): Flow<List<CaptureQueueEntity>>

    /**
     * 캡처 항목 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapture(capture: CaptureQueueEntity)

    /**
     * 캡처 항목 업데이트
     */
    @Update
    suspend fun updateCapture(capture: CaptureQueueEntity)

    /**
     * 동기화 상태만 업데이트
     */
    @Query("UPDATE capture_queue SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    /**
     * 동기화 상태 및 에러 메시지 업데이트
     */
    @Query("UPDATE capture_queue SET sync_status = :status, error_message = :error WHERE id = :id")
    suspend fun updateSyncStatusWithError(id: String, status: String, error: String)

    /**
     * 재시도 카운트 증가
     */
    @Query("""
        UPDATE capture_queue
        SET retry_count = retry_count + 1,
            last_retry_timestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun incrementRetryCount(id: String, timestamp: Long)

    /**
     * 특정 캡처 항목 삭제
     */
    @Query("DELETE FROM capture_queue WHERE id = :id")
    suspend fun deleteCapture(id: String)

    /**
     * 특정 상태의 캡처 항목들 삭제
     */
    @Query("DELETE FROM capture_queue WHERE sync_status = :status")
    suspend fun deleteCapturesByStatus(status: String)

    /**
     * 특정 상태의 캡처 개수 조회
     */
    @Query("SELECT COUNT(*) FROM capture_queue WHERE sync_status = :status")
    fun getPendingCount(status: String): Flow<Int>

    /**
     * 모든 캡처 항목 삭제 (테스트용)
     */
    @Query("DELETE FROM capture_queue")
    suspend fun deleteAll()
}
