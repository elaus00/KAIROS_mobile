package com.example.kairos_mobile.presentation.history

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.usecase.capture.GetAllCapturesUseCase
import com.example.kairos_mobile.domain.usecase.capture.HardDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.SoftDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.UndoDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import com.example.kairos_mobile.util.TestFixtures
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * HistoryViewModel 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAllCapturesUseCase: GetAllCapturesUseCase = mockk()
    private val softDeleteCaptureUseCase: SoftDeleteCaptureUseCase = mockk()
    private val hardDeleteCaptureUseCase: HardDeleteCaptureUseCase = mockk()
    private val undoDeleteCaptureUseCase: UndoDeleteCaptureUseCase = mockk()
    private val changeClassificationUseCase: ChangeClassificationUseCase = mockk()

    @Before
    fun setUp() {
        clearAllMocks()
    }

    /**
     * ViewModel 생성 헬퍼 — init 블록에서 loadFirstPage() 호출되므로
     * getAllCapturesUseCase(0, 20) mock이 반드시 먼저 설정되어야 함
     */
    private fun createViewModel(): HistoryViewModel {
        return HistoryViewModel(
            getAllCapturesUseCase,
            softDeleteCaptureUseCase,
            hardDeleteCaptureUseCase,
            undoDeleteCaptureUseCase,
            changeClassificationUseCase
        )
    }

    // ── 1. 초기 로드: 20개 항목 ──

    @Test
    fun `init_loads_first_page`() = runTest {
        // given: 페이지 0에 20개 캡처 반환
        val captures = (1..20).map { TestFixtures.capture(id = "cap-$it") }
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)

        // when
        val viewModel = createViewModel()
        advanceUntilIdle()

        // then
        assertEquals(20, viewModel.uiState.value.captures.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── 2. 풀 페이지 → hasMore == true ──

    @Test
    fun `hasMore_true_when_full_page`() = runTest {
        val captures = (1..20).map { TestFixtures.capture(id = "cap-$it") }
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasMore)
    }

    // ── 3. 부분 페이지 → hasMore == false ──

    @Test
    fun `hasMore_false_when_partial`() = runTest {
        val captures = (1..10).map { TestFixtures.capture(id = "cap-$it") }
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
    }

    // ── 4. loadMore → 페이지 1 데이터 추가 ──

    @Test
    fun `loadMore_appends_next_page`() = runTest {
        // given: 페이지 0 풀, 페이지 1 부분
        val page0 = (1..20).map { TestFixtures.capture(id = "cap-$it") }
        val page1 = (21..30).map { TestFixtures.capture(id = "cap-$it") }
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(page0)
        every { getAllCapturesUseCase(offset = 20, limit = 20) } returns flowOf(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.loadMore()
        advanceUntilIdle()

        // then: 총 30개
        assertEquals(30, viewModel.uiState.value.captures.size)
    }

    // ── 5. isLoadingMore 중 중복 loadMore 방지 ──

    @Test
    fun `loadMore_prevents_duplicate`() = runTest {
        val page0 = (1..20).map { TestFixtures.capture(id = "cap-$it") }
        val page1 = (21..40).map { TestFixtures.capture(id = "cap-$it") }
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(page0)
        every { getAllCapturesUseCase(offset = 20, limit = 20) } returns flowOf(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when: 연속 두 번 호출
        viewModel.loadMore()
        viewModel.loadMore()
        advanceUntilIdle()

        // then: 페이지 1만 한 번 로드 — 총 40개
        assertEquals(40, viewModel.uiState.value.captures.size)
    }

    // ── 6. 중복 ID 필터링 ──

    @Test
    fun `captures_distinctBy_id`() = runTest {
        // given: 페이지 0과 1에 겹치는 ID 포함
        val page0 = (1..20).map { TestFixtures.capture(id = "cap-$it") }
        val page1 = listOf(
            TestFixtures.capture(id = "cap-20"),  // 중복
            TestFixtures.capture(id = "cap-21")
        )
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(page0)
        every { getAllCapturesUseCase(offset = 20, limit = 20) } returns flowOf(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        // then: cap-20은 한 번만 → 총 21개
        assertEquals(21, viewModel.uiState.value.captures.size)
    }

    // ── 7. 삭제 → softDelete + UI 제거 + DeleteSuccess 이벤트 ──

    @Test
    fun `delete_soft_deletes_and_emits`() = runTest {
        val captures = listOf(
            TestFixtures.capture(id = "cap-1"),
            TestFixtures.capture(id = "cap-2")
        )
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery { softDeleteCaptureUseCase("cap-1") } returns Unit
        coEvery { hardDeleteCaptureUseCase(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.events.test {
            viewModel.deleteCaptureById("cap-1")
            advanceUntilIdle()

            // then: softDelete 호출됨
            coVerify { softDeleteCaptureUseCase("cap-1") }
            // UI에서 제거됨
            assertEquals(1, viewModel.uiState.value.captures.size)
            assertEquals("cap-2", viewModel.uiState.value.captures[0].id)
            // DeleteSuccess 이벤트 발생
            val event = awaitItem()
            assertTrue(event is HistoryEvent.DeleteSuccess)
            assertEquals("cap-1", (event as HistoryEvent.DeleteSuccess).captureId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 8. 3초 지연 후 hardDelete 실행 ──

    @Test
    fun `delete_schedules_hard_delete_3s`() = runTest {
        val captures = listOf(TestFixtures.capture(id = "cap-1"))
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery { softDeleteCaptureUseCase("cap-1") } returns Unit
        coEvery { hardDeleteCaptureUseCase("cap-1") } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteCaptureById("cap-1")
        runCurrent()  // softDelete 실행 (delay 작업은 대기 상태 유지)

        // 2초 후 → hardDelete 아직 미실행
        advanceTimeBy(2000)
        coVerify(exactly = 0) { hardDeleteCaptureUseCase(any()) }

        // 추가 1.5초 (총 3.5초) → hardDelete 실행
        advanceTimeBy(1500)
        coVerify(exactly = 1) { hardDeleteCaptureUseCase("cap-1") }
    }

    // ── 9. undoDelete → hardDelete 취소 + undoDeleteCapture + 새로고침 ──

    @Test
    fun `undo_cancels_hard_delete`() = runTest {
        val captures = listOf(TestFixtures.capture(id = "cap-1"))
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery { softDeleteCaptureUseCase("cap-1") } returns Unit
        coEvery { hardDeleteCaptureUseCase("cap-1") } returns Unit
        coEvery { undoDeleteCaptureUseCase("cap-1") } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        // 삭제 실행
        viewModel.deleteCaptureById("cap-1")
        runCurrent()  // softDelete 실행 (hardDelete delay는 아직 실행되지 않음)

        // 실행 취소
        viewModel.events.test {
            viewModel.undoDelete("cap-1")
            runCurrent()

            // then: undoDelete 호출됨
            coVerify { undoDeleteCaptureUseCase("cap-1") }
            // hardDelete 예약 취소 확인
            advanceTimeBy(3500)
            coVerify(exactly = 0) { hardDeleteCaptureUseCase("cap-1") }
            // UndoSuccess 이벤트 발생
            val event = awaitItem()
            assertTrue(event is HistoryEvent.UndoSuccess)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 10. changeClassification → UseCase 위임 ──

    @Test
    fun `changeClassification_delegates`() = runTest {
        val captures = listOf(TestFixtures.capture(id = "cap-1"))
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery {
            changeClassificationUseCase("cap-1", ClassifiedType.TODO, null)
        } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.changeClassification("cap-1", ClassifiedType.TODO, null)
        advanceUntilIdle()

        // then
        coVerify { changeClassificationUseCase("cap-1", ClassifiedType.TODO, null) }
    }

    // ── 11. changeClassification 에러 → errorMessage 설정 ──

    @Test
    fun `changeClassification_error`() = runTest {
        val captures = listOf(TestFixtures.capture(id = "cap-1"))
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery {
            changeClassificationUseCase("cap-1", ClassifiedType.NOTES, NoteSubType.IDEA)
        } throws RuntimeException("분류 변경 실패")

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.changeClassification("cap-1", ClassifiedType.NOTES, NoteSubType.IDEA)
        advanceUntilIdle()

        // then
        assertEquals("분류 변경 실패", viewModel.uiState.value.errorMessage)
    }

    // ── 12. onCleared → 대기 중인 hardDelete 즉시 실행 ──

    @Test
    fun `onCleared_executes_pending_deletes`() = runTest {
        val captures = listOf(
            TestFixtures.capture(id = "cap-1"),
            TestFixtures.capture(id = "cap-2")
        )
        every { getAllCapturesUseCase(offset = 0, limit = 20) } returns flowOf(captures)
        coEvery { softDeleteCaptureUseCase(any()) } returns Unit
        coEvery { hardDeleteCaptureUseCase(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        // 두 개 삭제 → 각각 3초 대기 중인 hardDelete 스케줄
        viewModel.deleteCaptureById("cap-1")
        viewModel.deleteCaptureById("cap-2")
        advanceUntilIdle()

        // onCleared 호출 (리플렉션으로 protected 메서드 접근)
        viewModel::class.java.getDeclaredMethod("onCleared").apply {
            isAccessible = true
        }.invoke(viewModel)

        // then: 두 캡처 모두 즉시 hardDelete 실행
        coVerify { hardDeleteCaptureUseCase("cap-1") }
        coVerify { hardDeleteCaptureUseCase("cap-2") }
    }
}
