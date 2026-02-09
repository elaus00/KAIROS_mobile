package com.example.kairos_mobile.domain.usecase.calendar

import com.example.kairos_mobile.domain.model.CalendarConflict
import com.example.kairos_mobile.domain.model.ConflictResolution
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import javax.inject.Inject

/** Google Calendar 충돌 해결 */
class ResolveCalendarConflictUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(conflict: CalendarConflict, resolution: ConflictResolution) {
        when (resolution) {
            ConflictResolution.OVERRIDE_LOCAL -> {
                scheduleRepository.updateFromRemote(
                    scheduleId = conflict.scheduleId,
                    title = conflict.googleTitle,
                    startTime = conflict.googleStartTime,
                    endTime = conflict.googleEndTime,
                    location = conflict.googleLocation
                )
            }
            ConflictResolution.OVERRIDE_GOOGLE -> {
                calendarRepository.syncToCalendar(
                    scheduleId = conflict.scheduleId,
                    title = conflict.localTitle,
                    startTime = conflict.localStartTime,
                    endTime = conflict.localEndTime,
                    location = conflict.localLocation,
                    isAllDay = false
                )
            }
            ConflictResolution.MERGE -> {
                // 기본: Google 데이터 우선 (나중에 UI에서 필드별 선택 가능)
                scheduleRepository.updateFromRemote(
                    scheduleId = conflict.scheduleId,
                    title = conflict.googleTitle,
                    startTime = conflict.googleStartTime,
                    endTime = conflict.googleEndTime,
                    location = conflict.googleLocation
                )
            }
        }
    }
}
