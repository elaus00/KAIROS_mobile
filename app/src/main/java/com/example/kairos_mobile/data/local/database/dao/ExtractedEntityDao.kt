package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.ExtractedEntityEntity

/**
 * 추출 엔티티 DAO
 */
@Dao
interface ExtractedEntityDao {

    /**
     * 추출 엔티티 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExtractedEntityEntity)

    /**
     * 여러 추출 엔티티 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExtractedEntityEntity>)

    /**
     * 캡처별 추출 엔티티 조회 (suspend)
     */
    @Query("SELECT * FROM entities WHERE capture_id = :captureId")
    suspend fun getEntitiesForCaptureSync(captureId: String): List<ExtractedEntityEntity>

    /**
     * 캡처의 모든 추출 엔티티 삭제
     */
    @Query("DELETE FROM entities WHERE capture_id = :captureId")
    suspend fun deleteAllForCapture(captureId: String)
}
