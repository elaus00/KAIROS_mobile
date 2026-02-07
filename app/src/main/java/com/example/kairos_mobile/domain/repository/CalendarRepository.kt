package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.CalendarSyncStatus

/**
 * Google Calendar 동기화 Repository 인터페이스
 */
interface CalendarRepository {
    /** 일정을 Google Calendar에 동기화 */
    suspend fun syncToCalendar(scheduleId: String, title: String, startTime: Long, endTime: Long?, location: String?, isAllDay: Boolean): String
    /** Google Calendar 이벤트 삭제 */
    suspend fun deleteFromCalendar(googleEventId: String)
    /** 동기화 상태 업데이트 */
    suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, googleEventId: String? = null)
}
