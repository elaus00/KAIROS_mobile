package com.example.kairos_mobile.domain.usecase.calendar

import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.repository.CalendarRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더 제안 거부 UseCase
 */
@Singleton
class RejectCalendarSuggestionUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(scheduleId: String) {
        calendarRepository.updateSyncStatus(scheduleId, CalendarSyncStatus.REJECTED)
    }
}
