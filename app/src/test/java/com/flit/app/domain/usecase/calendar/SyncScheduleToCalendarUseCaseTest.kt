package com.flit.app.domain.usecase.calendar

import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.CaptureSource
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.ConfidenceLevel
import com.flit.app.domain.model.Schedule
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.usecase.settings.GetCalendarSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncScheduleToCalendarUseCaseTest {

    @Test
    fun calendar_disabled_sets_not_linked_and_stops() = runTest {
        val calendarRepository = mockk<CalendarRepository>(relaxed = true)
        val scheduleRepository = mockk<ScheduleRepository>()
        val captureRepository = mockk<CaptureRepository>()
        val settings = mockk<GetCalendarSettingsUseCase>()
        val notifier = mockk<CalendarNotifier>(relaxed = true)

        val schedule = Schedule(id = "sch-1", captureId = "cap-1", confidence = ConfidenceLevel.HIGH, startTime = 1000L)
        val capture = Capture(id = "cap-1", originalText = "회의", classifiedType = ClassifiedType.SCHEDULE, source = CaptureSource.APP)
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns schedule
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { settings.isCalendarEnabled() } returns false

        val useCase = SyncScheduleToCalendarUseCase(calendarRepository, scheduleRepository, captureRepository, settings, notifier)
        useCase("sch-1")

        coVerify(exactly = 1) { calendarRepository.updateSyncStatus("sch-1", CalendarSyncStatus.NOT_LINKED) }
        coVerify(exactly = 0) { calendarRepository.syncToCalendar(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun suggest_mode_marks_pending_and_notifies() = runTest {
        val calendarRepository = mockk<CalendarRepository>(relaxed = true)
        val scheduleRepository = mockk<ScheduleRepository>()
        val captureRepository = mockk<CaptureRepository>()
        val settings = mockk<GetCalendarSettingsUseCase>()
        val notifier = mockk<CalendarNotifier>(relaxed = true)

        val schedule = Schedule(id = "sch-1", captureId = "cap-1", confidence = ConfidenceLevel.HIGH, startTime = 1000L)
        val capture = Capture(id = "cap-1", originalText = "회의", classifiedType = ClassifiedType.SCHEDULE, source = CaptureSource.APP)
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns schedule
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { settings.isCalendarEnabled() } returns true
        coEvery { settings.getCalendarMode() } returns "suggest"
        coEvery { settings.isNotificationEnabled() } returns true

        val useCase = SyncScheduleToCalendarUseCase(calendarRepository, scheduleRepository, captureRepository, settings, notifier)
        useCase("sch-1")

        coVerify(exactly = 1) { calendarRepository.updateSyncStatus("sch-1", CalendarSyncStatus.SUGGESTION_PENDING) }
        coVerify(exactly = 1) { notifier.notifySuggestion(any()) }
    }

    @Test
    fun auto_mode_high_confidence_syncs_and_notifies() = runTest {
        val calendarRepository = mockk<CalendarRepository>(relaxed = true)
        val scheduleRepository = mockk<ScheduleRepository>()
        val captureRepository = mockk<CaptureRepository>()
        val settings = mockk<GetCalendarSettingsUseCase>()
        val notifier = mockk<CalendarNotifier>(relaxed = true)

        val schedule = Schedule(id = "sch-1", captureId = "cap-1", confidence = ConfidenceLevel.HIGH, startTime = 1000L)
        val capture = Capture(id = "cap-1", originalText = "회의", classifiedType = ClassifiedType.SCHEDULE, source = CaptureSource.APP)
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns schedule
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { settings.isCalendarEnabled() } returns true
        coEvery { settings.getCalendarMode() } returns "auto"
        coEvery { settings.isNotificationEnabled() } returns true
        coEvery { calendarRepository.syncToCalendar(any(), any(), any(), any(), any(), any()) } returns "event-1"

        val useCase = SyncScheduleToCalendarUseCase(calendarRepository, scheduleRepository, captureRepository, settings, notifier)
        useCase("sch-1")

        coVerify(exactly = 1) { calendarRepository.syncToCalendar("sch-1", any(), 1000L, any(), any(), any()) }
        coVerify(exactly = 1) { notifier.notifyAutoSync(any()) }
    }

    @Test
    fun auto_mode_medium_confidence_also_syncs() = runTest {
        val calendarRepository = mockk<CalendarRepository>(relaxed = true)
        val scheduleRepository = mockk<ScheduleRepository>()
        val captureRepository = mockk<CaptureRepository>()
        val settings = mockk<GetCalendarSettingsUseCase>()
        val notifier = mockk<CalendarNotifier>(relaxed = true)

        val schedule = Schedule(id = "sch-1", captureId = "cap-1", confidence = ConfidenceLevel.MEDIUM, startTime = 1000L)
        val capture = Capture(id = "cap-1", originalText = "회의", classifiedType = ClassifiedType.SCHEDULE, source = CaptureSource.APP)
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns schedule
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { settings.isCalendarEnabled() } returns true
        coEvery { settings.getCalendarMode() } returns "auto"
        coEvery { settings.isNotificationEnabled() } returns true
        coEvery { calendarRepository.syncToCalendar(any(), any(), any(), any(), any(), any()) } returns "event-1"

        val useCase = SyncScheduleToCalendarUseCase(calendarRepository, scheduleRepository, captureRepository, settings, notifier)
        useCase("sch-1")

        coVerify(exactly = 1) { calendarRepository.syncToCalendar("sch-1", any(), 1000L, any(), any(), any()) }
        coVerify(exactly = 1) { notifier.notifyAutoSync(any()) }
    }

    @Test
    fun auto_mode_sync_failure_sets_failed() = runTest {
        val calendarRepository = mockk<CalendarRepository>(relaxed = true)
        val scheduleRepository = mockk<ScheduleRepository>()
        val captureRepository = mockk<CaptureRepository>()
        val settings = mockk<GetCalendarSettingsUseCase>()
        val notifier = mockk<CalendarNotifier>(relaxed = true)

        val schedule = Schedule(id = "sch-1", captureId = "cap-1", confidence = ConfidenceLevel.HIGH, startTime = 1000L)
        val capture = Capture(id = "cap-1", originalText = "회의", classifiedType = ClassifiedType.SCHEDULE, source = CaptureSource.APP)
        coEvery { scheduleRepository.getScheduleById("sch-1") } returns schedule
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { settings.isCalendarEnabled() } returns true
        coEvery { settings.getCalendarMode() } returns "auto"
        coEvery { settings.isNotificationEnabled() } returns false
        coEvery { calendarRepository.syncToCalendar(any(), any(), any(), any(), any(), any()) } throws RuntimeException("sync fail")

        val useCase = SyncScheduleToCalendarUseCase(calendarRepository, scheduleRepository, captureRepository, settings, notifier)
        useCase("sch-1")

        coVerify(exactly = 1) { calendarRepository.updateSyncStatus("sch-1", CalendarSyncStatus.SYNC_FAILED) }
        coVerify(exactly = 0) { notifier.notifyAutoSync(any()) }
    }
}
