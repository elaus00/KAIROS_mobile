package com.example.kairos_mobile.domain.usecase.calendar

import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Calendar 이벤트 삭제 UseCase
 */
@Singleton
class DeleteCalendarEventUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String) {
        val schedule = scheduleRepository.getScheduleById(scheduleId) ?: return
        val eventId = schedule.googleEventId ?: return
        calendarRepository.deleteFromCalendar(eventId)
        calendarRepository.updateSyncStatus(scheduleId, CalendarSyncStatus.NOT_LINKED)
    }
}
