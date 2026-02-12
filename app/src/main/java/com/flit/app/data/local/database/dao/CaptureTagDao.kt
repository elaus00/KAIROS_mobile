package com.flit.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flit.app.data.local.database.entities.CaptureTagEntity

/**
 * 캡처-태그 관계 DAO
 */
@Dao
interface CaptureTagDao {

    /**
     * 캡처-태그 관계 삽입
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(captureTag: CaptureTagEntity)

    /**
     * 여러 캡처-태그 관계 삽입
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(captureTags: List<CaptureTagEntity>)

    /**
     * 캡처의 모든 태그 관계 삭제
     */
    @Query("DELETE FROM capture_tags WHERE capture_id = :captureId")
    suspend fun deleteAllForCapture(captureId: String)

    /**
     * 특정 캡처-태그 관계 삭제
     */
    @Query("DELETE FROM capture_tags WHERE capture_id = :captureId AND tag_id = :tagId")
    suspend fun delete(captureId: String, tagId: String)

    /**
     * 캡처에 연결된 태그 이름 목록 조회
     */
    @Query("""
        SELECT t.name FROM tags t
        INNER JOIN capture_tags ct ON t.id = ct.tag_id
        WHERE ct.capture_id = :captureId
    """)
    suspend fun getTagNamesByCaptureId(captureId: String): List<String>
}
