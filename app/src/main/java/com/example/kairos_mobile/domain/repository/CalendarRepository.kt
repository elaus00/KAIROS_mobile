package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.RemoteCalendarEvent
import java.time.LocalDate

/**
 * Google Calendar 동기화 Repository 인터페이스
 */
interface CalendarRepository {
    /** 일정을 Google Calendar에 동기화 */
    suspend fun syncToCalendar(scheduleId: String, title: String, startTime: Long, endTime: Long?, location: String?, isAllDay: Boolean): String
    /** 동기화 상태 업데이트 */
    suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, googleEventId: String? = null)
    /** OAuth code 교환 + 토큰 저장 */
    suspend fun exchangeCalendarToken(code: String, redirectUri: String): Boolean
    /** 토큰 직접 저장 */
    suspend fun saveCalendarToken(accessToken: String, refreshToken: String?, expiresAt: String?): Boolean
    /** 서버 캘린더 이벤트 조회 */
    suspend fun getCalendarEvents(startDate: LocalDate, endDate: LocalDate): List<RemoteCalendarEvent>
}
