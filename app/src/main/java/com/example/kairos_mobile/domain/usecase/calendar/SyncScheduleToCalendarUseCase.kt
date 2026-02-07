package com.example.kairos_mobile.domain.usecase.calendar

import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 발행 인터페이스 (Domain 계층에서 NotificationHelper 직접 참조 방지)
 */
interface CalendarNotifier {
    fun notifySuggestion(scheduleTitle: String)
    fun notifyAutoSync(scheduleTitle: String)
}

/**
 * 일정을 Google Calendar에 동기화하는 UseCase
 * confidence 기반 AUTO/SUGGEST 분기 + 알림 발행
 */
@Singleton
class SyncScheduleToCalendarUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val scheduleRepository: ScheduleRepository,
    private val captureRepository: CaptureRepository,
    private val calendarNotifier: CalendarNotifier
) {
    suspend operator fun invoke(scheduleId: String) {
        val schedule = scheduleRepository.getScheduleById(scheduleId) ?: return
        val capture = captureRepository.getCaptureById(schedule.captureId) ?: return
        val title = capture.aiTitle ?: capture.originalText.take(30)

        when (schedule.confidence) {
            ConfidenceLevel.HIGH -> {
                // 자동 동기화
                try {
                    calendarRepository.syncToCalendar(
                        scheduleId = scheduleId,
                        title = title,
                        startTime = schedule.startTime ?: return,
                        endTime = schedule.endTime,
                        location = schedule.location,
                        isAllDay = schedule.isAllDay
                    )
                    calendarNotifier.notifyAutoSync(title)
                } catch (e: Exception) {
                    calendarRepository.updateSyncStatus(scheduleId, CalendarSyncStatus.SYNC_FAILED)
                }
            }
            else -> {
                // MEDIUM/LOW → 제안 상태로 전환 + 알림
                calendarRepository.updateSyncStatus(scheduleId, CalendarSyncStatus.SUGGESTION_PENDING)
                calendarNotifier.notifySuggestion(title)
            }
        }
    }
}
