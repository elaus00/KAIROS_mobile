package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kairos_mobile.data.local.database.entities.AnalyticsEventEntity

/**
 * 분석 이벤트 DAO
 */
@Dao
interface AnalyticsEventDao {

    /**
     * 이벤트 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AnalyticsEventEntity)

    /**
     * 동기화되지 않은 이벤트 조회
     */
    @Query("SELECT * FROM analytics_events WHERE is_synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 50): List<AnalyticsEventEntity>

    /**
     * 동기화 완료 표시
     */
    @Query("UPDATE analytics_events SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    /**
     * 오래된 동기화 완료 이벤트 삭제
     */
    @Query("DELETE FROM analytics_events WHERE is_synced = 1 AND timestamp < :threshold")
    suspend fun deleteOld(threshold: Long)
}
