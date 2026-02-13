package com.flit.app.domain.repository

import com.flit.app.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

/**
 * 일정 Repository 인터페이스
 */
interface ScheduleRepository {

    /** 일정 생성 */
    suspend fun createSchedule(schedule: Schedule)

    /** 일정 조회 (id 기준) */
    suspend fun getScheduleById(id: String): Schedule?

    /** 일정 조회 (capture_id 기준) */
    suspend fun getScheduleByCaptureId(captureId: String): Schedule?

    /** 특정 날짜의 일정 조회 (start_time 기준) */
    fun getSchedulesByDate(dateStartMs: Long, dateEndMs: Long): Flow<List<Schedule>>

    /** 일정이 있는 날짜 목록 조회 (캘린더 도트 표시용) */
    fun getDatesWithSchedules(rangeStartMs: Long, rangeEndMs: Long): Flow<List<Long>>

    /** 일정 업데이트 */
    suspend fun updateSchedule(schedule: Schedule)

    /** capture_id로 삭제 */
    suspend fun deleteByCaptureId(captureId: String)
}
