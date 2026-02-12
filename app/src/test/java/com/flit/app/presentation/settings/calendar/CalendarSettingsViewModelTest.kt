package com.flit.app.presentation.settings.calendar

import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.usecase.settings.GetCalendarSettingsUseCase
import com.flit.app.domain.usecase.settings.SetCalendarSettingsUseCase
import com.flit.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CalendarSettingsViewModel 단위 테스트
 * - 캘린더 설정 로드, 자동추가 토글, 캘린더 선택 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var calendarRepository: CalendarRepository
    private lateinit var getCalendarSettingsUseCase: GetCalendarSettingsUseCase
    private lateinit var setCalendarSettingsUseCase: SetCalendarSettingsUseCase

    @Before
    fun setUp() {
        calendarRepository = mockk(relaxed = true)
        getCalendarSettingsUseCase = mockk(relaxed = true)
        setCalendarSettingsUseCase = mockk(relaxed = true)
        coEvery { calendarRepository.getAvailableCalendars() } returns emptyList()
        coEvery { calendarRepository.getTargetCalendarId() } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createViewModel(): CalendarSettingsViewModel {
        return CalendarSettingsViewModel(
            calendarRepository,
            getCalendarSettingsUseCase,
            setCalendarSettingsUseCase
        )
    }

    /** init 시 캘린더 설정을 로드한다 */
    @Test
    fun init_loads_calendar_settings() = runTest {
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "auto"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns false

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isAutoAddEnabled)
        assertEquals(false, viewModel.uiState.value.isNotificationEnabled)
    }

    /** toggleAutoAdd(true) 시 setCalendarMode("auto") 호출 */
    @Test
    fun toggleAutoAdd_true_sets_auto_mode() = runTest {
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleAutoAdd(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setCalendarMode("auto") }
        assertEquals(true, viewModel.uiState.value.isAutoAddEnabled)
    }

    /** toggleAutoAdd(false) 시 setCalendarMode("suggest") 호출 */
    @Test
    fun toggleAutoAdd_false_sets_suggest_mode() = runTest {
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "auto"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleAutoAdd(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setCalendarMode("suggest") }
        assertEquals(false, viewModel.uiState.value.isAutoAddEnabled)
    }

    /** setTargetCalendar 호출 시 calendarRepository에 위임한다 */
    @Test
    fun setTargetCalendar_delegates_to_repository() = runTest {
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTargetCalendar(42L)
        advanceUntilIdle()

        coVerify { calendarRepository.setTargetCalendarId(42L) }
        assertEquals(42L, viewModel.uiState.value.selectedCalendarId)
    }
}
