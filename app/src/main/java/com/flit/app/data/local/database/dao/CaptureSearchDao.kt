package com.flit.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.flit.app.data.local.database.entities.CaptureEntity
import com.flit.app.data.local.database.entities.CaptureSearchFts
import kotlinx.coroutines.flow.Flow

/**
 * 캡처 전문 검색 DAO (FTS4)
 */
@Dao
interface CaptureSearchDao {

    /**
     * FTS 인덱스 삽입
     */
    @Insert
    suspend fun insert(fts: CaptureSearchFts)

    /**
     * FTS 인덱스 삭제 (캡처 삭제 시)
     */
    @Query("DELETE FROM capture_search WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    /**
     * FTS 인덱스 업데이트 (삭제 후 재삽입)
     */
    @Query("DELETE FROM capture_search WHERE capture_id = :captureId")
    suspend fun deleteForUpdate(captureId: String)

    /**
     * 전문 검색 (FTS4 MATCH)
     * 결과를 captures 테이블과 조인하여 CaptureEntity 반환
     */
    @Query("""
        SELECT c.* FROM captures c
        INNER JOIN capture_search cs ON c.id = cs.capture_id
        WHERE capture_search MATCH :query
        AND c.is_deleted = 0
        ORDER BY c.created_at DESC
        LIMIT :limit
    """)
    fun search(query: String, limit: Int = 50): Flow<List<CaptureEntity>>

    /**
     * 필터 포함 전문 검색 (FTS4 MATCH + 분류 유형 + 날짜 범위)
     * 휴지통 항목 제외
     */
    @Query("""
        SELECT c.* FROM captures c
        INNER JOIN capture_search cs ON c.id = cs.capture_id
        WHERE capture_search MATCH :query
        AND c.is_deleted = 0 AND c.is_trashed = 0
        AND (:type IS NULL OR c.classified_type = :type)
        AND (:startDate IS NULL OR c.created_at >= :startDate)
        AND (:endDate IS NULL OR c.created_at <= :endDate)
        ORDER BY c.created_at DESC
        LIMIT :limit
    """)
    suspend fun searchFiltered(
        query: String,
        type: String?,
        startDate: Long?,
        endDate: Long?,
        limit: Int = 50
    ): List<CaptureEntity>
}
