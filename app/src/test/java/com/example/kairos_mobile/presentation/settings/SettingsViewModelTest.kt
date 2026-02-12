package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.AuthRepository
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.settings.GetCalendarSettingsUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetCalendarSettingsUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * SettingsViewModel 단위 테스트
 * - 테마 설정 로드/변경/에러 닫기/캘린더 토글 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var getCalendarSettingsUseCase: GetCalendarSettingsUseCase
    private lateinit var setCalendarSettingsUseCase: SetCalendarSettingsUseCase
    private lateinit var submitCaptureUseCase: SubmitCaptureUseCase
    private lateinit var imageRepository: ImageRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Before
    fun setUp() {
        userPreferenceRepository = mockk()
        calendarRepository = mockk(relaxed = true)
        getCalendarSettingsUseCase = mockk(relaxed = true)
        setCalendarSettingsUseCase = mockk(relaxed = true)
        submitCaptureUseCase = mockk(relaxed = true)
        imageRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        subscriptionRepository = mockk(relaxed = true)
        every { calendarRepository.isCalendarPermissionGranted() } returns true
        coEvery { userPreferenceRepository.getString(any(), any()) } answers { secondArg() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            userPreferenceRepository,
            calendarRepository,
            getCalendarSettingsUseCase,
            setCalendarSettingsUseCase,
            submitCaptureUseCase,
            imageRepository,
            authRepository,
            subscriptionRepository
        )
    }

    /** init 시 테마 설정을 로드하여 uiState에 반영한다 */
    @Test
    fun init_loads_theme_preference() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ThemePreference.DARK, viewModel.uiState.value.themePreference)
    }

    /** setTheme 호출 시 UserPreferenceRepository에 위임한다 */
    @Test
    fun setTheme_delegates_to_usecase() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { userPreferenceRepository.setThemePreference(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTheme(ThemePreference.LIGHT)
        advanceUntilIdle()

        coVerify { userPreferenceRepository.setThemePreference(ThemePreference.LIGHT) }
        assertEquals(ThemePreference.LIGHT, viewModel.uiState.value.themePreference)
    }

    /** onErrorDismissed 호출 시 errorMessage를 null로 초기화한다 */
    @Test
    fun onErrorDismissed_clears_error() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onErrorDismissed()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun init_loads_calendar_enabled() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isCalendarEnabled)
    }

    @Test
    fun toggleCalendar_delegates_to_usecase() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCalendar(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setCalendarEnabled(true) }
    }
}
