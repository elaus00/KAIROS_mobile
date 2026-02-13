package com.flit.app.presentation.capture

import android.app.Application
import android.os.Trace
import app.cash.turbine.test
import com.flit.app.domain.model.Capture
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.util.MainDispatcherRule
import com.flit.app.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CaptureViewModel 단위 테스트
 * - 초기화(임시저장 로드, 미확인 수 관찰)
 * - 입력 업데이트 / 최대 글자수 초과 방지
 * - 제출 성공/실패/빈값 처리
 * - 임시저장 / 상태 시트 토글
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var submitCaptureUseCase: SubmitCaptureUseCase
    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var captureRepository: CaptureRepository
    private lateinit var imageRepository: ImageRepository

    @Before
    fun setup() {
        // Trace 정적 메서드 모킹 (Android 프레임워크 의존성 제거)
        mockkStatic(Trace::class)
        every { Trace.beginSection(any()) } just runs
        every { Trace.endSection() } just runs

        application = mockk(relaxed = true)
        submitCaptureUseCase = mockk()
        userPreferenceRepository = mockk()
        captureRepository = mockk()
        imageRepository = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 기본 mock 설정 후 ViewModel 생성 헬퍼
     * init{}에서 userPreferenceRepository.getString + getUnconfirmedCount를 호출하므로 반드시 mock 필요
     */
    private fun createViewModel(
        draftText: String = "",
        draftImageUri: String = "",
        unconfirmedCount: Int = 0
    ): CaptureViewModel {
        coEvery { userPreferenceRepository.getString("draft_capture", "") } returns draftText
        coEvery { userPreferenceRepository.getString("draft_capture_image_uri", "") } returns draftImageUri
        every { captureRepository.getUnconfirmedCount() } returns flowOf(unconfirmedCount)
        return CaptureViewModel(
            application,
            submitCaptureUseCase,
            userPreferenceRepository,
            captureRepository,
            imageRepository
        )
    }

    // ========== 초기화 테스트 ==========

    @Test
    fun `init_loads_draft_into_input`() = runTest {
        // given: 임시저장에 "임시" 텍스트가 있을 때
        val vm = createViewModel(draftText = "임시")
        advanceUntilIdle()

        // then: inputText에 로드됨
        val state = vm.uiState.value
        assertEquals("임시", state.inputText)
    }

    @Test
    fun `init_blank_draft_no_update`() = runTest {
        // given: 임시저장이 비어있을 때
        val vm = createViewModel(draftText = "")
        advanceUntilIdle()

        // then: 기본 상태 유지 (빈 문자열)
        assertEquals("", vm.uiState.value.inputText)
    }

    @Test
    fun `init_observes_unconfirmed_count`() = runTest {
        // given: 미확인 분류가 3개일 때
        val vm = createViewModel(unconfirmedCount = 3)
        advanceUntilIdle()

        // then: unconfirmedCount 반영
        assertEquals(3, vm.uiState.value.unconfirmedCount)
    }

    // ========== 입력 업데이트 테스트 ==========

    @Test
    fun `updateInput_updates_text_and_count`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // when: "안녕" 입력
        vm.updateInput("안녕")

        // then: inputText 업데이트
        assertEquals("안녕", vm.uiState.value.inputText)
    }

    @Test
    fun `updateInput_rejects_over_max`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // given: 현재 입력값 설정
        vm.updateInput("기존")
        val stateBefore = vm.uiState.value

        // when: 5001자 입력 시도 (maxCharacterCount=5000 초과)
        val oversizedText = "가".repeat(5001)
        vm.updateInput(oversizedText)

        // then: 이전 상태 유지 (무시됨)
        assertEquals(stateBefore.inputText, vm.uiState.value.inputText)
    }

    // ========== 제출 테스트 ==========

    @Test
    fun `submit_success_clears_and_emits`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // given: 텍스트 입력 후 제출 준비
        vm.updateInput("캡처 내용")
        coEvery { submitCaptureUseCase(any(), any()) } returns TestFixtures.capture()
        coEvery { userPreferenceRepository.setString("draft_capture", "") } just runs
        coEvery { userPreferenceRepository.setString("draft_capture_image_uri", "") } just runs

        // when: 이벤트 수집 시작 + 제출
        vm.events.test {
            vm.submit()
            advanceUntilIdle()

            // then: SubmitSuccess 이벤트 발생
            assertEquals(CaptureEvent.SubmitSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // then: 입력 초기화 + deleteDraft 호출
        assertEquals("", vm.uiState.value.inputText)
        assertFalse(vm.uiState.value.isSubmitting)
        coVerify(exactly = 1) { userPreferenceRepository.setString("draft_capture", "") }
        coVerify(exactly = 1) { userPreferenceRepository.setString("draft_capture_image_uri", "") }
    }

    @Test
    fun `submit_blank_does_nothing`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // when: 빈 상태에서 제출
        vm.submit()
        advanceUntilIdle()

        // then: submitCaptureUseCase 호출되지 않음
        coVerify(exactly = 0) { submitCaptureUseCase(any(), any()) }
    }

    @Test
    fun `submit_error_sets_message`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // given: 텍스트 입력 + 제출 시 예외 발생 설정
        vm.updateInput("에러 테스트")
        coEvery { submitCaptureUseCase(any(), any()) } throws RuntimeException("서버 오류")

        // when: 제출
        vm.submit()
        advanceUntilIdle()

        // then: 에러 메시지 설정 + isSubmitting false
        assertEquals("서버 오류", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    // ========== 임시저장 테스트 ==========

    @Test
    fun `saveDraft_saves_text`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // given: 텍스트 입력
        vm.updateInput("저장할 내용")
        coEvery { userPreferenceRepository.setString("draft_capture", any()) } just runs
        coEvery { userPreferenceRepository.setString("draft_capture_image_uri", any()) } just runs

        // when: 임시 저장
        vm.saveDraft()
        advanceUntilIdle()

        // then: userPreferenceRepository.setString 호출됨
        coVerify(exactly = 1) { userPreferenceRepository.setString("draft_capture", "저장할 내용") }
        coVerify(exactly = 1) { userPreferenceRepository.setString("draft_capture_image_uri", "") }
    }

    @Test
    fun `saveDraft_blank_skips`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // when: 빈 상태에서 임시 저장
        vm.saveDraft()
        advanceUntilIdle()

        // then: userPreferenceRepository.setString 호출되지 않음
        coVerify(exactly = 0) { userPreferenceRepository.setString(any(), any()) }
    }

    // ========== UI 상태 토글 테스트 ==========

    @Test
    fun `toggleStatusSheet_toggles`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // 초기 상태: false
        assertFalse(vm.uiState.value.showStatusSheet)

        // when: 토글 1회 → true
        vm.toggleStatusSheet()
        assertTrue(vm.uiState.value.showStatusSheet)

        // when: 토글 2회 → false
        vm.toggleStatusSheet()
        assertFalse(vm.uiState.value.showStatusSheet)
    }
}
