package com.example.kairos_mobile.domain.usecase.calendar

import com.example.kairos_mobile.domain.model.CalendarConflict
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import java.time.LocalDate
import javax.inject.Inject

/** Google Calendar 충돌 감지 */
class DetectCalendarConflictsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(): List<CalendarConflict> {
        val today = LocalDate.now()
        val remoteEvents = calendarRepository.getCalendarEvents(today.minusDays(7), today.plusDays(30))
        val localSchedules = scheduleRepository.getSyncedSchedules()

        val conflicts = mutableListOf<CalendarConflict>()
        for (schedule in localSchedules) {
            val googleEventId = schedule.googleEventId ?: continue
            val localStartTime = schedule.startTime ?: continue
            val remote = remoteEvents.find { it.googleEventId == googleEventId } ?: continue
            val hasConflict = schedule.title != remote.title ||
                localStartTime != remote.startTime ||
                schedule.endTime != remote.endTime ||
                schedule.location != remote.location
            if (hasConflict) {
                conflicts.add(CalendarConflict(
                    scheduleId = schedule.id,
                    googleEventId = googleEventId,
                    localTitle = schedule.title,
                    googleTitle = remote.title,
                    localStartTime = localStartTime,
                    googleStartTime = remote.startTime,
                    localEndTime = schedule.endTime,
                    googleEndTime = remote.endTime,
                    localLocation = schedule.location,
                    googleLocation = remote.location
                ))
            }
        }
        return conflicts
    }
}
