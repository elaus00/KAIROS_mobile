package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.CaptureEntity
import kotlinx.coroutines.flow.Flow

/**
 * 캡처 DAO
 */
@Dao
interface CaptureDao {

    /**
     * 캡처 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capture: CaptureEntity)

    /**
     * 캡처 업데이트
     */
    @Update
    suspend fun update(capture: CaptureEntity)

    /**
     * ID로 캡처 조회
     */
    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun getById(id: String): CaptureEntity?

    /**
     * 활성 캡처 조회 (삭제되지 않은 항목, 최신순, 페이징)
     */
    @Query("""
        SELECT * FROM captures
        WHERE is_deleted = 0
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getActiveCapturesPaged(limit: Int, offset: Int): Flow<List<CaptureEntity>>

    /**
     * 분류 유형별 캡처 조회
     */
    @Query("""
        SELECT * FROM captures
        WHERE classified_type = :type AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun getCapturesByType(type: String): Flow<List<CaptureEntity>>

    /**
     * 미확인 분류 캡처 조회 (AI Status Sheet용)
     * TEMP 제외, 삭제되지 않은 항목
     */
    @Query("""
        SELECT * FROM captures
        WHERE is_confirmed = 0
        AND classified_type != 'TEMP'
        AND is_deleted = 0
        AND COALESCE(classification_completed_at, created_at) >= (strftime('%s', 'now', '-1 day') * 1000)
        ORDER BY created_at DESC
    """)
    fun getUnconfirmedCaptures(): Flow<List<CaptureEntity>>

    /**
     * 미확인 분류 캡처 수 조회
     */
    @Query("""
        SELECT COUNT(*) FROM captures
        WHERE is_confirmed = 0
        AND classified_type != 'TEMP'
        AND is_deleted = 0
        AND COALESCE(classification_completed_at, created_at) >= (strftime('%s', 'now', '-1 day') * 1000)
    """)
    fun getUnconfirmedCount(): Flow<Int>

    /**
     * TEMP 상태 캡처 조회 (재분류 대상)
     */
    @Query("""
        SELECT * FROM captures
        WHERE classified_type = 'TEMP'
        AND is_deleted = 0
        ORDER BY created_at ASC
    """)
    suspend fun getTempCaptures(): List<CaptureEntity>

    /**
     * 소프트 삭제
     */
    @Query("""
        UPDATE captures
        SET is_deleted = 1, deleted_at = :deletedAt, updated_at = :deletedAt
        WHERE id = :id
    """)
    suspend fun softDelete(id: String, deletedAt: Long)

    /**
     * 소프트 삭제 취소
     */
    @Query("""
        UPDATE captures
        SET is_deleted = 0, deleted_at = NULL, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun undoSoftDelete(id: String, updatedAt: Long)

    /**
     * 하드 삭제 (DB 완전 삭제)
     */
    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun hardDelete(id: String)

    /**
     * 분류 유형 업데이트
     */
    @Query("""
        UPDATE captures
        SET classified_type = :classifiedType,
            note_sub_type = :noteSubType,
            classification_completed_at = :completedAt,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateClassification(
        id: String,
        classifiedType: String,
        noteSubType: String?,
        completedAt: Long?,
        updatedAt: Long
    )

    /**
     * AI 분류 확인 처리
     */
    @Query("""
        UPDATE captures
        SET is_confirmed = 1, confirmed_at = :confirmedAt, updated_at = :confirmedAt
        WHERE id = :id
    """)
    suspend fun confirmClassification(id: String, confirmedAt: Long)

    /**
     * 미확인 분류 전체 확인
     */
    @Query("""
        UPDATE captures
        SET is_confirmed = 1, confirmed_at = :confirmedAt, updated_at = :confirmedAt
        WHERE is_confirmed = 0
        AND classified_type != 'TEMP'
        AND is_deleted = 0
        AND COALESCE(classification_completed_at, created_at) >= (strftime('%s', 'now', '-1 day') * 1000)
    """)
    suspend fun confirmAllClassifications(confirmedAt: Long)

    /**
     * AI 제목 업데이트
     */
    @Query("""
        UPDATE captures
        SET ai_title = :aiTitle, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateAiTitle(id: String, aiTitle: String, updatedAt: Long)

    /**
     * 신뢰도 업데이트
     */
    @Query("""
        UPDATE captures
        SET confidence = :confidence, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateConfidence(id: String, confidence: String, updatedAt: Long)

    /**
     * 전체 캡처 개수 (삭제되지 않은 항목)
     */
    @Query("SELECT COUNT(*) FROM captures WHERE is_deleted = 0")
    fun getActiveCount(): Flow<Int>

    /**
     * 특정 폴더에 속한 노트들의 캡처 note_sub_type 일괄 변경
     * 폴더 삭제 시 소속 캡처의 note_sub_type을 INBOX로 변경하기 위해 사용
     */
    @Query("""
        UPDATE captures
        SET note_sub_type = :noteSubType, updated_at = :updatedAt
        WHERE id IN (
            SELECT capture_id FROM notes WHERE folder_id = :folderId
        )
    """)
    suspend fun updateNoteSubTypeByFolderId(folderId: String, noteSubType: String, updatedAt: Long)

    /**
     * 모든 캡처 삭제 (테스트용)
     */
    @Query("DELETE FROM captures")
    suspend fun deleteAll()
}
