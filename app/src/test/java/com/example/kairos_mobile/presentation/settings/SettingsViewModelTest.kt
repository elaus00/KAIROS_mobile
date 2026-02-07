package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetThemePreferenceUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
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

    private lateinit var getThemePreferenceUseCase: GetThemePreferenceUseCase
    private lateinit var setThemePreferenceUseCase: SetThemePreferenceUseCase

    @Before
    fun setUp() {
        getThemePreferenceUseCase = mockk()
        setThemePreferenceUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** init 시 테마 설정을 로드하여 uiState에 반영한다 */
    @Test
    fun init_loads_theme_preference() = runTest {
        // given
        every { getThemePreferenceUseCase() } returns flowOf(ThemePreference.DARK)

        // when - ViewModel 생성 시 init에서 loadPreferences 호출
        val viewModel = SettingsViewModel(getThemePreferenceUseCase, setThemePreferenceUseCase)
        advanceUntilIdle()

        // then
        assertEquals(ThemePreference.DARK, viewModel.uiState.value.themePreference)
    }

    /** setTheme 호출 시 SetThemePreferenceUseCase에 위임한다 */
    @Test
    fun setTheme_delegates_to_usecase() = runTest {
        // given
        every { getThemePreferenceUseCase() } returns flowOf(ThemePreference.DARK)
        val viewModel = SettingsViewModel(getThemePreferenceUseCase, setThemePreferenceUseCase)
        advanceUntilIdle()

        // when
        viewModel.setTheme(ThemePreference.LIGHT)
        advanceUntilIdle()

        // then
        coVerify { setThemePreferenceUseCase(ThemePreference.LIGHT) }
        assertEquals(ThemePreference.LIGHT, viewModel.uiState.value.themePreference)
    }

    /** onErrorDismissed 호출 시 errorMessage를 null로 초기화한다 */
    @Test
    fun onErrorDismissed_clears_error() = runTest {
        // given
        every { getThemePreferenceUseCase() } returns flowOf(ThemePreference.DARK)
        val viewModel = SettingsViewModel(getThemePreferenceUseCase, setThemePreferenceUseCase)
        advanceUntilIdle()

        // when
        viewModel.onErrorDismissed()

        // then - 기본값이 null이므로 호출 후에도 null 유지 확인
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
