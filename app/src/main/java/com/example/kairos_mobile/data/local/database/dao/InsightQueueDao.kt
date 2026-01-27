package com.example.kairos_mobile.data.local.database.dao

import androidx.room.*
import com.example.kairos_mobile.data.local.database.entities.InsightQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * 인사이트 큐 Data Access Object
 */
@Dao
interface InsightQueueDao {

    /**
     * 특정 상태의 인사이트 항목들 조회
     */
    @Query("SELECT * FROM insight_queue WHERE sync_status = :status ORDER BY timestamp DESC")
    fun getInsightsByStatus(status: String): Flow<List<InsightQueueEntity>>

    /**
     * 최근 인사이트 항목들 조회
     */
    @Query("SELECT * FROM insight_queue ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInsights(limit: Int = 50): Flow<List<InsightQueueEntity>>

    /**
     * 인사이트 항목 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightQueueEntity)

    /**
     * 인사이트 항목 업데이트
     */
    @Update
    suspend fun updateInsight(insight: InsightQueueEntity)

    /**
     * 동기화 상태만 업데이트
     */
    @Query("UPDATE insight_queue SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    /**
     * 동기화 상태 및 에러 메시지 업데이트
     */
    @Query("UPDATE insight_queue SET sync_status = :status, error_message = :error WHERE id = :id")
    suspend fun updateSyncStatusWithError(id: String, status: String, error: String)

    /**
     * 재시도 카운트 증가
     */
    @Query("""
        UPDATE insight_queue
        SET retry_count = retry_count + 1,
            last_retry_timestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun incrementRetryCount(id: String, timestamp: Long)

    /**
     * 특정 인사이트 항목 삭제
     */
    @Query("DELETE FROM insight_queue WHERE id = :id")
    suspend fun deleteInsight(id: String)

    /**
     * 특정 상태의 인사이트 항목들 삭제
     */
    @Query("DELETE FROM insight_queue WHERE sync_status = :status")
    suspend fun deleteInsightsByStatus(status: String)

    /**
     * 특정 상태의 인사이트 개수 조회
     */
    @Query("SELECT COUNT(*) FROM insight_queue WHERE sync_status = :status")
    fun getPendingCount(status: String): Flow<Int>

    /**
     * 모든 인사이트 항목 삭제 (테스트용)
     */
    @Query("DELETE FROM insight_queue")
    suspend fun deleteAll()

    /**
     * 검색 쿼리로 인사이트 항목 조회 (페이징 지원)
     *
     * @param searchText 검색할 텍스트 (content에서 검색)
     * @param types 필터링할 타입들 (비어있으면 모든 타입)
     * @param sources 필터링할 소스들 (비어있으면 모든 소스)
     * @param startDate 시작 날짜 (null이면 제한 없음)
     * @param endDate 종료 날짜 (null이면 제한 없음)
     * @param limit 페이지 크기
     * @param offset 시작 위치
     */
    @Query("""
        SELECT * FROM insight_queue
        WHERE (:searchText = '' OR content LIKE '%' || :searchText || '%'
               OR title LIKE '%' || :searchText || '%')
        AND (:types IS NULL OR :types = '' OR classification_type IN (:typesList))
        AND (:sources IS NULL OR :sources = '' OR source IN (:sourcesList))
        AND (:startDate IS NULL OR timestamp >= :startDate)
        AND (:endDate IS NULL OR timestamp <= :endDate)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    fun searchInsights(
        searchText: String,
        types: String?,
        typesList: List<String>?,
        sources: String?,
        sourcesList: List<String>?,
        startDate: Long?,
        endDate: Long?,
        limit: Int,
        offset: Int
    ): Flow<List<InsightQueueEntity>>

    /**
     * 모든 인사이트 항목 조회 (페이징 지원)
     */
    @Query("SELECT * FROM insight_queue ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getAllInsightsPaged(limit: Int, offset: Int): Flow<List<InsightQueueEntity>>

    /**
     * 특정 ID로 인사이트 항목 조회
     */
    @Query("SELECT * FROM insight_queue WHERE id = :id")
    suspend fun getInsightById(id: String): InsightQueueEntity?

    /**
     * 전체 인사이트 개수 조회
     */
    @Query("SELECT COUNT(*) FROM insight_queue")
    fun getTotalCount(): Flow<Int>
}
