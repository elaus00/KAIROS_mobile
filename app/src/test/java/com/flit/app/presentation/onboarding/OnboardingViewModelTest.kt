package com.flit.app.presentation.onboarding

import app.cash.turbine.test
import com.flit.app.domain.model.LocalCalendar
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.domain.usecase.settings.CalendarSettingsKeys
import com.flit.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * OnboardingViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var submitCaptureUseCase: SubmitCaptureUseCase
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        userPreferenceRepository = mockk(relaxed = true)
        submitCaptureUseCase = mockk(relaxed = true)
        calendarRepository = mockk(relaxed = true)
        every { calendarRepository.isCalendarPermissionGranted() } returns false
        coEvery { calendarRepository.getAvailableCalendars() } returns emptyList()
        viewModel = OnboardingViewModel(userPreferenceRepository, submitCaptureUseCase, calendarRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun updateInput_updates_text() = runTest {
        viewModel.updateInput("텍스트")
        assertEquals("텍스트", viewModel.uiState.value.inputText)
    }

    @Test
    fun completeOnboarding_submits_and_navigates() = runTest {
        viewModel.updateInput("첫 번째 캡처")

        viewModel.events.test {
            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify { submitCaptureUseCase("첫 번째 캡처") }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    @Test
    fun completeOnboarding_blank_skips_capture() = runTest {
        viewModel.events.test {
            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify(exactly = 0) { submitCaptureUseCase(any()) }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    @Test
    fun completeOnboarding_swallows_capture_error() = runTest {
        viewModel.updateInput("에러 캡처")
        coEvery { submitCaptureUseCase(any()) } throws RuntimeException("네트워크 오류")

        viewModel.events.test {
            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    @Test
    fun skip_completes_without_capture() = runTest {
        viewModel.events.test {
            viewModel.skip()
            advanceUntilIdle()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify(exactly = 0) { submitCaptureUseCase(any()) }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    @Test
    fun isSubmitting_false_after_completion() = runTest {
        viewModel.updateInput("제출 중 테스트")
        viewModel.completeOnboarding()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun onCalendarPermissionResult_granted_updates_state_and_persists() = runTest {
        coEvery { calendarRepository.getAvailableCalendars() } returns listOf(
            LocalCalendar(
                id = 42L,
                displayName = "Primary",
                accountName = "test@gmail.com",
                color = 0,
                isPrimary = true
            )
        )

        viewModel.onCalendarPermissionResult(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true") }
        coVerify(exactly = 1) { calendarRepository.setTargetCalendarId(42L) }
        assertTrue(viewModel.uiState.value.isCalendarPermissionGranted)
        assertEquals(null, viewModel.uiState.value.calendarConnectionError)
    }

    @Test
    fun onCalendarPermissionResult_denied_sets_error_and_disables_calendar() = runTest {
        viewModel.onCalendarPermissionResult(false)
        advanceUntilIdle()

        coVerify(exactly = 2) { userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "false") }
        assertFalse(viewModel.uiState.value.isCalendarPermissionGranted)
        assertEquals(
            "권한이 없어도 계속 사용할 수 있습니다. 나중에 설정에서 켜세요.",
            viewModel.uiState.value.calendarConnectionError
        )
    }

    @Test
    fun onCalendarPermissionResult_retry_clears_error() = runTest {
        viewModel.onCalendarPermissionResult(false)
        advanceUntilIdle()

        coEvery { calendarRepository.getAvailableCalendars() } returns emptyList()
        viewModel.onCalendarPermissionResult(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCalendarPermissionGranted)
        assertEquals(null, viewModel.uiState.value.calendarConnectionError)
    }
}
