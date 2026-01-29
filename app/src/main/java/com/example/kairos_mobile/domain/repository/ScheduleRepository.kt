package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.ScheduleCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Schedule Repository 인터페이스 (PRD v4.0)
 */
interface ScheduleRepository {

    /**
     * 특정 날짜의 일정 조회
     */
    fun getSchedulesByDate(date: LocalDate): Flow<List<Schedule>>

    /**
     * 특정 기간의 일정 조회
     */
    fun getSchedulesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Schedule>>

    /**
     * 오늘 이후 일정 조회
     */
    fun getUpcomingSchedules(): Flow<List<Schedule>>

    /**
     * 모든 일정 조회
     */
    fun getAllSchedules(): Flow<List<Schedule>>

    /**
     * 카테고리별 일정 조회
     */
    fun getSchedulesByCategory(category: ScheduleCategory): Flow<List<Schedule>>

    /**
     * 일정이 있는 날짜 목록 조회 (Calendar 점 표시용)
     */
    fun getDatesWithSchedules(startDate: LocalDate, endDate: LocalDate): Flow<List<LocalDate>>

    /**
     * 새 일정 생성
     */
    suspend fun createSchedule(schedule: Schedule): Result<Schedule>

    /**
     * 캡처에서 일정 생성
     */
    suspend fun createScheduleFromCapture(
        captureId: String,
        title: String,
        date: LocalDate,
        time: java.time.LocalTime,
        location: String? = null,
        category: ScheduleCategory = ScheduleCategory.PERSONAL
    ): Result<Schedule>

    /**
     * 일정 업데이트
     */
    suspend fun updateSchedule(schedule: Schedule): Result<Schedule>

    /**
     * 일정 삭제
     */
    suspend fun deleteSchedule(id: String): Result<Unit>

    /**
     * ID로 일정 조회
     */
    suspend fun getScheduleById(id: String): Result<Schedule?>

    /**
     * Google Calendar ID로 일정 조회
     */
    suspend fun getScheduleByGoogleCalendarId(googleCalendarId: String): Result<Schedule?>

    /**
     * 특정 날짜의 일정 개수 조회
     */
    suspend fun getScheduleCountByDate(date: LocalDate): Int
}
