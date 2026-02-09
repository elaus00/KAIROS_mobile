package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.ClassificationLogEntity

/**
 * 분류 수정 로그 DAO
 */
@Dao
interface ClassificationLogDao {

    /**
     * 로그 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ClassificationLogEntity)

    /**
     * 특정 캡처의 분류 로그 조회
     */
    @Query("SELECT * FROM classification_logs WHERE capture_id = :captureId ORDER BY modified_at DESC")
    suspend fun getByCaptureId(captureId: String): List<ClassificationLogEntity>

    /**
     * 전체 분류 로그 조회
     */
    @Query("SELECT * FROM classification_logs ORDER BY modified_at DESC")
    suspend fun getAll(): List<ClassificationLogEntity>

}
