package com.flit.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flit.app.data.local.database.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 일정 DAO
 */
@Dao
interface ScheduleDao {

    /**
     * 일정 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity)

    /**
     * 일정 업데이트
     */
    @Update
    suspend fun update(schedule: ScheduleEntity)

    /**
     * ID로 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: String): ScheduleEntity?

    /**
     * 캡처 ID로 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE capture_id = :captureId")
    suspend fun getByCaptureId(captureId: String): ScheduleEntity?

    /**
     * 특정 날짜의 일정 조회 (start_time 기준, epoch ms)
     */
    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN captures c ON c.id = s.capture_id
        WHERE s.start_time >= :startOfDay
        AND s.start_time < :endOfDay
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY s.start_time ASC
    """)
    fun getSchedulesByDate(startOfDay: Long, endOfDay: Long): Flow<List<ScheduleEntity>>

    /**
     * 특정 기간의 일정 조회
     */
    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN captures c ON c.id = s.capture_id
        WHERE s.start_time >= :startTime
        AND s.start_time <= :endTime
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY s.start_time ASC
    """)
    fun getSchedulesBetween(startTime: Long, endTime: Long): Flow<List<ScheduleEntity>>

    /**
     * 일정이 있는 날짜 목록 조회 (캘린더 점 표시용)
     * start_time을 epoch day로 변환하여 distinct
     */
    @Query("""
        SELECT DISTINCT (s.start_time / 86400000) AS epoch_day
        FROM schedules s
        INNER JOIN captures c ON c.id = s.capture_id
        WHERE s.start_time >= :startTime
        AND s.start_time <= :endTime
        AND c.is_deleted = 0
        AND c.is_trashed = 0
    """)
    fun getDatesWithSchedules(startTime: Long, endTime: Long): Flow<List<Long>>

    /**
     * 캡처 ID로 일정 삭제
     */
    @Query("DELETE FROM schedules WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 캘린더 동기화 상태로 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE calendar_sync_status = :status")
    suspend fun getByCalendarSyncStatus(status: String): List<ScheduleEntity>

    /**
     * 캘린더 동기화 상태 업데이트
     */
    @Query("""
        UPDATE schedules
        SET calendar_sync_status = :status, google_event_id = :calendarEventId, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateCalendarSync(id: String, status: String, calendarEventId: String?, updatedAt: Long = System.currentTimeMillis())

    /** 동기화용 전체 일정 조회 */
    @Query("SELECT * FROM schedules")
    suspend fun getAllForSync(): List<ScheduleEntity>
}
