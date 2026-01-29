package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Schedule DAO (PRD v4.0)
 * 일정 데이터베이스 접근
 */
@Dao
interface ScheduleDao {

    /**
     * 새 일정 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity)

    /**
     * 일정 업데이트
     */
    @Update
    suspend fun update(schedule: ScheduleEntity)

    /**
     * 일정 삭제
     */
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * ID로 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: String): ScheduleEntity?

    /**
     * 특정 날짜의 일정 조회 (epoch day)
     * 시간순 정렬
     */
    @Query("""
        SELECT * FROM schedules
        WHERE date = :epochDay
        ORDER BY time ASC
    """)
    fun getSchedulesByDate(epochDay: Long): Flow<List<ScheduleEntity>>

    /**
     * 특정 기간의 일정 조회
     * startDate, endDate는 epoch day
     */
    @Query("""
        SELECT * FROM schedules
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date ASC, time ASC
    """)
    fun getSchedulesBetweenDates(startDate: Long, endDate: Long): Flow<List<ScheduleEntity>>

    /**
     * 오늘 이후 일정 조회
     */
    @Query("""
        SELECT * FROM schedules
        WHERE date >= :todayEpochDay
        ORDER BY date ASC, time ASC
    """)
    fun getUpcomingSchedules(todayEpochDay: Long): Flow<List<ScheduleEntity>>

    /**
     * 모든 일정 조회
     */
    @Query("SELECT * FROM schedules ORDER BY date ASC, time ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    /**
     * 카테고리별 일정 조회
     */
    @Query("""
        SELECT * FROM schedules
        WHERE category = :category
        ORDER BY date ASC, time ASC
    """)
    fun getSchedulesByCategory(category: String): Flow<List<ScheduleEntity>>

    /**
     * Google Calendar ID로 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE google_calendar_id = :googleCalendarId")
    suspend fun getByGoogleCalendarId(googleCalendarId: String): ScheduleEntity?

    /**
     * 캡처 ID로 연결된 일정 조회
     */
    @Query("SELECT * FROM schedules WHERE source_capture_id = :captureId")
    suspend fun getScheduleByCaptureId(captureId: String): ScheduleEntity?

    /**
     * 특정 날짜의 일정 개수 조회
     */
    @Query("SELECT COUNT(*) FROM schedules WHERE date = :epochDay")
    suspend fun getScheduleCountByDate(epochDay: Long): Int

    /**
     * 특정 기간의 일정이 있는 날짜 목록 조회
     * Calendar 점 표시용
     */
    @Query("""
        SELECT DISTINCT date FROM schedules
        WHERE date >= :startDate AND date <= :endDate
    """)
    fun getDatesWithSchedules(startDate: Long, endDate: Long): Flow<List<Long>>
}
