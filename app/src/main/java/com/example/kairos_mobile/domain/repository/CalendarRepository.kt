package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.LocalCalendar
import com.example.kairos_mobile.domain.model.CalendarSyncStatus

/**
 * 기기 캘린더 동기화 Repository 인터페이스
 */
interface CalendarRepository {
    /** 일정을 기기 캘린더에 동기화 */
    suspend fun syncToCalendar(scheduleId: String, title: String, startTime: Long, endTime: Long?, location: String?, isAllDay: Boolean): String
    /** 동기화 상태 업데이트 */
    suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, calendarEventId: String? = null)
    /** 캘린더 권한 허용 여부 */
    fun isCalendarPermissionGranted(): Boolean
    /** 사용 가능한 기기 캘린더 목록 */
    suspend fun getAvailableCalendars(): List<LocalCalendar>
    /** 대상 캘린더 선택 */
    suspend fun setTargetCalendarId(calendarId: Long)
    /** 선택된 대상 캘린더 조회 */
    suspend fun getTargetCalendarId(): Long?
}
