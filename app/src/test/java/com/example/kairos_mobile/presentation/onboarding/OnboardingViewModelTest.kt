package com.example.kairos_mobile.presentation.onboarding

import app.cash.turbine.test
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.settings.CalendarSettingsKeys
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
 * - 입력 업데이트, 온보딩 완료/스킵, 에러 처리 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var submitCaptureUseCase: SubmitCaptureUseCase
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        userPreferenceRepository = mockk(relaxed = true)
        submitCaptureUseCase = mockk(relaxed = true)
        coEvery {
            userPreferenceRepository.getString(
                CalendarSettingsKeys.KEY_CALENDAR_ENABLED,
                "false"
            )
        } returns "false"
        viewModel = OnboardingViewModel(userPreferenceRepository, submitCaptureUseCase)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** updateInput 호출 시 inputText가 업데이트된다 */
    @Test
    fun updateInput_updates_text() = runTest {
        // when
        viewModel.updateInput("텍스트")

        // then
        assertEquals("텍스트", viewModel.uiState.value.inputText)
    }

    /** completeOnboarding 호출 시 캡처 제출 + 온보딩 완료 + NavigateToHome 이벤트 발생 */
    @Test
    fun completeOnboarding_submits_and_navigates() = runTest {
        // given
        viewModel.updateInput("첫 번째 캡처")

        // then - 이벤트 수집
        viewModel.events.test {
            // when
            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        // then - UseCase 호출 확인
        coVerify { submitCaptureUseCase("첫 번째 캡처") }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    /** 빈 텍스트로 completeOnboarding 호출 시 캡처 제출 없이 온보딩 완료 */
    @Test
    fun completeOnboarding_blank_skips_capture() = runTest {
        // given - 빈 텍스트 (기본값)

        // then - 이벤트 수집
        viewModel.events.test {
            // when
            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        // then - submitCapture 호출되지 않음
        coVerify(exactly = 0) { submitCaptureUseCase(any()) }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    /** submitCaptureUseCase 예외 발생 시에도 온보딩 완료 + NavigateToHome 이벤트 발생 */
    @Test
    fun completeOnboarding_swallows_capture_error() = runTest {
        // given
        viewModel.updateInput("에러 캡처")
        coEvery { submitCaptureUseCase(any()) } throws RuntimeException("네트워크 오류")

        // then - 이벤트 수집
        viewModel.events.test {
            // when
            viewModel.completeOnboarding()
            advanceUntilIdle()

            // then - 예외에도 불구하고 네비게이션 이벤트 발생
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    /** skip 호출 시 캡처 제출 없이 온보딩 완료 + NavigateToHome 이벤트 발생 */
    @Test
    fun skip_completes_without_capture() = runTest {
        // then - 이벤트 수집
        viewModel.events.test {
            // when
            viewModel.skip()
            advanceUntilIdle()

            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        // then - submitCapture 호출되지 않음
        coVerify(exactly = 0) { submitCaptureUseCase(any()) }
        coVerify { userPreferenceRepository.setOnboardingCompleted() }
    }

    /** completeOnboarding 완료 후 isSubmitting이 false로 복귀한다 */
    @Test
    fun isSubmitting_false_after_completion() = runTest {
        // given
        viewModel.updateInput("제출 중 테스트")

        // when
        viewModel.completeOnboarding()

        // then - advanceUntilIdle 전에 isSubmitting 확인 (코루틴 실행 중)
        // StandardTestDispatcher이므로 launch 내부 로직은 아직 실행 안 됨
        // advanceUntilIdle 후에는 이미 완료되므로 중간 상태를 검증
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun connectGoogle_persists_and_updates_state() = runTest {
        viewModel.connectGoogle()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
        }
        assertTrue(viewModel.uiState.value.isGoogleConnected)
    }

    /** connectGoogle 실패 시 에러 메시지가 설정된다 */
    @Test
    fun connectGoogle_failure_sets_error_message() = runTest {
        // given - setString 호출 시 예외 발생
        coEvery {
            userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
        } throws RuntimeException("연결 실패")

        // when
        viewModel.connectGoogle()
        advanceUntilIdle()

        // then - 에러 메시지 설정됨
        assertEquals("연결에 실패했습니다. 다시 시도해주세요.", viewModel.uiState.value.googleConnectionError)
        assertFalse(viewModel.uiState.value.isGoogleConnected)
    }

    /** connectGoogle 재시도 시 에러 메시지가 초기화된다 */
    @Test
    fun connectGoogle_retry_clears_error() = runTest {
        // given - 첫 시도 실패
        coEvery {
            userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
        } throws RuntimeException("연결 실패")
        viewModel.connectGoogle()
        advanceUntilIdle()
        assertEquals("연결에 실패했습니다. 다시 시도해주세요.", viewModel.uiState.value.googleConnectionError)

        // when - 재시도 (성공)
        coEvery {
            userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
        } returns Unit
        viewModel.connectGoogle()
        advanceUntilIdle()

        // then - 에러 메시지 초기화됨
        assertEquals(null, viewModel.uiState.value.googleConnectionError)
        assertTrue(viewModel.uiState.value.isGoogleConnected)
    }
}
