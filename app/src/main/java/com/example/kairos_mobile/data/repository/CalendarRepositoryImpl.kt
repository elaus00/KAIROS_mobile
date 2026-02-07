package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.repository.CalendarRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalendarRepository 구현체
 * Mock API를 통한 Google Calendar 동기화
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    private val scheduleDao: ScheduleDao
) : CalendarRepository {

    override suspend fun syncToCalendar(
        scheduleId: String,
        title: String,
        startTime: Long,
        endTime: Long?,
        location: String?,
        isAllDay: Boolean
    ): String {
        val request = CalendarEventRequest(
            title = title,
            startTime = startTime,
            endTime = endTime,
            location = location,
            isAllDay = isAllDay
        )
        val response = api.createCalendarEvent(request)
        if (response.isSuccessful) {
            val eventId = response.body()!!.googleEventId
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNCED.name, eventId)
            return eventId
        } else {
            scheduleDao.updateCalendarSync(scheduleId, CalendarSyncStatus.SYNC_FAILED.name, null)
            throw Exception("캘린더 동기화 실패: ${response.code()}")
        }
    }

    override suspend fun deleteFromCalendar(googleEventId: String) {
        api.deleteCalendarEvent(googleEventId)
    }

    override suspend fun updateSyncStatus(scheduleId: String, status: CalendarSyncStatus, googleEventId: String?) {
        scheduleDao.updateCalendarSync(scheduleId, status.name, googleEventId)
    }
}
