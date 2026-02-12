package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.AuthRepository
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.classification.GetPresetsUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetCustomInstructionUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetPresetUseCase
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
 * - 테마 설정 로드/변경/에러 닫기 검증
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
    private lateinit var getPresetsUseCase: GetPresetsUseCase
    private lateinit var setPresetUseCase: SetPresetUseCase
    private lateinit var setCustomInstructionUseCase: SetCustomInstructionUseCase

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
        getPresetsUseCase = mockk(relaxed = true)
        setPresetUseCase = mockk(relaxed = true)
        setCustomInstructionUseCase = mockk(relaxed = true)
        every { calendarRepository.isCalendarPermissionGranted() } returns true
        coEvery { calendarRepository.getAvailableCalendars() } returns emptyList()
        coEvery { calendarRepository.getTargetCalendarId() } returns null
        // loadPresets()에서 호출되는 getString mock 설정
        coEvery { userPreferenceRepository.getString(any(), any()) } answers { secondArg() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** init 시 테마 설정을 로드하여 uiState에 반영한다 */
    @Test
    fun init_loads_theme_preference() = runTest {
        // given
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        // when - ViewModel 생성 시 init에서 loadPreferences 호출
        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        // then
        assertEquals(ThemePreference.DARK, viewModel.uiState.value.themePreference)
    }

    /** setTheme 호출 시 UserPreferenceRepository에 위임한다 */
    @Test
    fun setTheme_delegates_to_usecase() = runTest {
        // given
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true
        coEvery { userPreferenceRepository.setThemePreference(any()) } returns Unit
        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        // when
        viewModel.setTheme(ThemePreference.LIGHT)
        advanceUntilIdle()

        // then
        coVerify { userPreferenceRepository.setThemePreference(ThemePreference.LIGHT) }
        assertEquals(ThemePreference.LIGHT, viewModel.uiState.value.themePreference)
    }

    /** onErrorDismissed 호출 시 errorMessage를 null로 초기화한다 */
    @Test
    fun onErrorDismissed_clears_error() = runTest {
        // given
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.DARK)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true
        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        // when
        viewModel.onErrorDismissed()

        // then - 기본값이 null이므로 호출 후에도 null 유지 확인
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun init_loads_calendar_settings() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns true
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "auto"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns false

        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isCalendarEnabled)
        assertEquals("auto", viewModel.uiState.value.calendarMode)
        assertEquals(false, viewModel.uiState.value.isNotificationEnabled)
    }

    @Test
    fun toggleCalendar_delegates_to_usecase() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        viewModel.toggleCalendar(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setCalendarEnabled(true) }
    }

    @Test
    fun setCalendarMode_delegates_to_usecase() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        viewModel.setCalendarMode("auto")
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setCalendarMode("auto") }
    }

    @Test
    fun toggleNotification_delegates_to_usecase() = runTest {
        every { userPreferenceRepository.getThemePreference() } returns flowOf(ThemePreference.SYSTEM)
        coEvery { getCalendarSettingsUseCase.isCalendarEnabled() } returns false
        coEvery { getCalendarSettingsUseCase.getCalendarMode() } returns "suggest"
        coEvery { getCalendarSettingsUseCase.isNotificationEnabled() } returns true

        val viewModel = SettingsViewModel(userPreferenceRepository, calendarRepository, getCalendarSettingsUseCase, setCalendarSettingsUseCase, submitCaptureUseCase, imageRepository, authRepository, subscriptionRepository, getPresetsUseCase, setPresetUseCase, setCustomInstructionUseCase)
        advanceUntilIdle()

        viewModel.toggleNotification(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { setCalendarSettingsUseCase.setNotificationEnabled(false) }
    }
}
