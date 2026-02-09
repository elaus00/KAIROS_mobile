package com.example.kairos_mobile.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.usecase.calendar.ApproveCalendarSuggestionUseCase
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.capture.FormatCaptureForShareUseCase
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import com.example.kairos_mobile.util.TestFixtures
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CaptureDetailViewModel 단위 테스트
 * - 캡처 로드, 분류 변경, 에러 처리 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CaptureDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var captureRepository: CaptureRepository
    private lateinit var changeClassificationUseCase: ChangeClassificationUseCase
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var approveSuggestion: ApproveCalendarSuggestionUseCase
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var trackEventUseCase: TrackEventUseCase
    private lateinit var formatCaptureForShare: FormatCaptureForShareUseCase

    @Before
    fun setUp() {
        captureRepository = mockk()
        changeClassificationUseCase = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        approveSuggestion = mockk(relaxed = true)
        calendarRepository = mockk(relaxed = true)
        trackEventUseCase = mockk(relaxed = true)
        formatCaptureForShare = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** init 시 SavedStateHandle의 captureId로 캡처를 로드하여 uiState에 반영한다 */
    @Test
    fun init_loads_capture_by_id() = runTest {
        // given
        val capture = TestFixtures.capture(
            id = "cap-1",
            originalText = "테스트 캡처",
            aiTitle = "테스트 제목",
            classifiedType = ClassifiedType.TODO,
            createdAt = 1000L
        )
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture

        // when
        val viewModel = CaptureDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("captureId" to "cap-1")),
            captureRepository = captureRepository,
            changeClassification = changeClassificationUseCase,
            scheduleRepository = scheduleRepository,
            approveSuggestion = approveSuggestion,
            calendarRepository = calendarRepository,
            trackEventUseCase = trackEventUseCase,
            formatCaptureForShare = formatCaptureForShare
        )
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("cap-1", state.captureId)
        assertEquals("테스트 캡처", state.originalText)
        assertEquals("테스트 제목", state.aiTitle)
        assertEquals(ClassifiedType.TODO, state.classifiedType)
        assertEquals(1000L, state.createdAt)
        assertNull(state.errorMessage)
    }

    /** 캡처를 찾을 수 없을 때 에러 메시지가 설정된다 */
    @Test
    fun capture_not_found_sets_error() = runTest {
        // given
        coEvery { captureRepository.getCaptureById("cap-999") } returns null

        // when
        val viewModel = CaptureDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("captureId" to "cap-999")),
            captureRepository = captureRepository,
            changeClassification = changeClassificationUseCase,
            scheduleRepository = scheduleRepository,
            approveSuggestion = approveSuggestion,
            calendarRepository = calendarRepository,
            trackEventUseCase = trackEventUseCase,
            formatCaptureForShare = formatCaptureForShare
        )
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("캡처를 찾을 수 없습니다", state.errorMessage)
    }

    /** onChangeClassification 호출 시 분류 유형이 변경된다 */
    @Test
    fun changeClassification_updates_state() = runTest {
        // given
        val capture = TestFixtures.capture(id = "cap-1", classifiedType = ClassifiedType.TEMP)
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        val viewModel = CaptureDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("captureId" to "cap-1")),
            captureRepository = captureRepository,
            changeClassification = changeClassificationUseCase,
            scheduleRepository = scheduleRepository,
            approveSuggestion = approveSuggestion,
            calendarRepository = calendarRepository,
            trackEventUseCase = trackEventUseCase,
            formatCaptureForShare = formatCaptureForShare
        )
        advanceUntilIdle()

        // when
        viewModel.onChangeClassification(ClassifiedType.SCHEDULE)
        advanceUntilIdle()

        // then
        assertEquals(ClassifiedType.SCHEDULE, viewModel.uiState.value.classifiedType)
        coVerify { changeClassificationUseCase("cap-1", ClassifiedType.SCHEDULE, null) }
    }

    /** changeClassification 예외 발생 시 에러 메시지가 설정된다 */
    @Test
    fun changeClassification_error_sets_message() = runTest {
        // given
        val capture = TestFixtures.capture(id = "cap-1")
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery {
            changeClassificationUseCase(any(), any(), any())
        } throws RuntimeException("분류 변경 실패")

        val viewModel = CaptureDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("captureId" to "cap-1")),
            captureRepository = captureRepository,
            changeClassification = changeClassificationUseCase,
            scheduleRepository = scheduleRepository,
            approveSuggestion = approveSuggestion,
            calendarRepository = calendarRepository,
            trackEventUseCase = trackEventUseCase,
            formatCaptureForShare = formatCaptureForShare
        )
        advanceUntilIdle()

        // when
        viewModel.onChangeClassification(ClassifiedType.NOTES, NoteSubType.IDEA)
        advanceUntilIdle()

        // then
        assertEquals("분류 변경 실패", viewModel.uiState.value.errorMessage)
    }

    /** onErrorDismissed 호출 시 에러 메시지가 초기화된다 */
    @Test
    fun dismissError_clears_message() = runTest {
        // given - 에러 상태 만들기 (존재하지 않는 캡처 로드)
        coEvery { captureRepository.getCaptureById("cap-1") } returns null
        val viewModel = CaptureDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("captureId" to "cap-1")),
            captureRepository = captureRepository,
            changeClassification = changeClassificationUseCase,
            scheduleRepository = scheduleRepository,
            approveSuggestion = approveSuggestion,
            calendarRepository = calendarRepository,
            trackEventUseCase = trackEventUseCase,
            formatCaptureForShare = formatCaptureForShare
        )
        advanceUntilIdle()
        assertEquals("캡처를 찾을 수 없습니다", viewModel.uiState.value.errorMessage)

        // when
        viewModel.onErrorDismissed()

        // then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
