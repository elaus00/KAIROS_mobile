package com.flit.app.domain.usecase.calendar

import com.flit.app.domain.model.CalendarException
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ScheduleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더 제안 승인 UseCase
 */
@Singleton
class ApproveCalendarSuggestionUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val scheduleRepository: ScheduleRepository,
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(scheduleId: String) {
        val schedule = scheduleRepository.getScheduleById(scheduleId) ?: return
        val capture = captureRepository.getCaptureById(schedule.captureId) ?: return
        val title = capture.aiTitle ?: capture.originalText.take(30)
        val startTime = schedule.startTime
            ?: throw CalendarException.InsertFailed("시작 시간이 없어서 캘린더에 추가할 수 없어요")

        calendarRepository.syncToCalendar(
            scheduleId = scheduleId,
            title = title,
            startTime = startTime,
            endTime = schedule.endTime,
            location = schedule.location,
            isAllDay = schedule.isAllDay
        )
    }
}
